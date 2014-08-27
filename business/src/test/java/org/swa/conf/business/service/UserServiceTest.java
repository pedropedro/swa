package org.swa.conf.business.service;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.business.mock.BasePersistenceBean;
import org.swa.conf.datatypes.User;

@RunWith(Arquillian.class)
public class UserServiceTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ArchiveProducer.createTestWebArchive();
		war.addClass(UserService.class);
		war.addClass(BasePersistenceBean.class);
		war.addClass(UserServiceTest.class);
		war.addAsLibraries(ArchiveProducer.pers.resolve("org.jasypt:jasypt:jar:lite:?").withTransitivity().asFile());
		war.addAsWebInfResource("ejb-jar.xml");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private UserService	s;

	@Test
	@InSequence(value = 10)
	public void notFound() {
		final User rec = s.findById(1L);
		assertNull(rec);
	}

	@Test
	@InSequence(value = 20)
	@Ignore
	public void crud() {

		final String digest = s.digest("password");

		final User dt = new User();
		dt.setPassword(digest);
		dt.setName("User 1");

		// Create
		s.save(dt);
		assertNotNull(dt.getId());

		// Read
		final User dt2 = s.findById(dt.getId());
		assertNotNull(dt2);
		assertEquals(dt, dt2);
		assertTrue(s.matches("password", dt2.getPassword()));

		// Update
		dt.setName("Speaker 2");
		s.save(dt);

		final User dt3 = s.findById(dt.getId());
		assertNotNull(dt3);
		assertEquals(dt, dt3);
		assertNotEquals(dt2, dt3);

		// Delete
		s.remove(dt);
		final User dt4 = s.findById(dt.getId());
		assertNull(dt4);
	}
}