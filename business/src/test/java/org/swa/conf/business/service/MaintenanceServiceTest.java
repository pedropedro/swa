package org.swa.conf.business.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.TimerHandle;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.swa.conf.business.events.PasswordExpiredEvent;
import org.swa.conf.business.events.TimerFiredEvent;
import org.swa.conf.business.events.TimerId;
import org.swa.conf.business.mock.BasePersistenceBean;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.User;

@RunWith(Arquillian.class)
@javax.ejb.Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class MaintenanceServiceTest {

	// -------------------------------------------------------------------------------------
	// There are two instances of this class in runtime - Arquillian's one and an EJB, which
	// only is able to receive CDI events, so we have to inject this EJB to our Arq. instance,
	// though not using @Inject (CDI doesn't like circular references :-) but @EJB ...
	// -------------------------------------------------------------------------------------

	@EJB
	private MaintenanceServiceTest	_selfAsEjbSingleton;

	private final List<User>				users	= new ArrayList<>();

	public List<User> getUsers() {
		return users;
	}

	public void receiver(@Observes final PasswordExpiredEvent e) {
		users.add(e.getUser());
		log.debug("Received user {} in {}", e.getUser(), this);
	}

	volatile private TimerHandle	timerHandle;

	public TimerHandle getTimerHandle() {
		return timerHandle;
	}

	public void receiver(@Observes @TimerId("generateReportForExpiredPasswords") final TimerFiredEvent e) {
		timerHandle = e.getTimerHandle();
		log.debug("Received timer handle {} in {}", timerHandle, this);
	}

	// -------------------------------------------------------------

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ArchiveProducer.createTestWebArchive();
		war.addClass(PasswordExpiredEvent.class);
		war.addClass(TimerFiredEvent.class);
		war.addClass(TimerId.class);
		war.addClass(MaintenanceService.class);
		war.addClass(BasePersistenceBean.class);
		war.addClass(MaintenanceServiceTest.class);
		war.addAsWebInfResource("maintenance-ejb-jar.xml", "ejb-jar.xml");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private Logger												log;

	@Inject
	private BasePersistenceService<User>	db;

	@Test
	public void scheduleTest() throws InterruptedException {

		final String expiredPwd = "Need passw change";

		User u = new User();
		u.setId("1");
		u.setName(expiredPwd);
		u.setPassword("very old");
		u.setLastPasswordChange(new Date(0));
		db.save(u);

		u = new User();
		u.setId("2");
		u.setName("No need to passw change");
		u.setPassword("fresh");
		u.setLastPasswordChange(new Date());
		db.save(u);

		u = new User();
		u.setId("3");
		u.setName(expiredPwd);
		u.setPassword("old");
		u.setLastPasswordChange(new Date(15000l * 24l * 60l * 60l * 1000l));
		db.save(u);

		while (_selfAsEjbSingleton.getUsers().size() < 2)
			TimeUnit.MILLISECONDS.sleep(100);

		assertEquals(2, _selfAsEjbSingleton.getUsers().size());

		for (final User user : _selfAsEjbSingleton.getUsers()) {
			assertEquals(expiredPwd, user.getName());
			assertNull(user.getPassword());
		}

		assertNotNull(_selfAsEjbSingleton.getTimerHandle());
	}
}