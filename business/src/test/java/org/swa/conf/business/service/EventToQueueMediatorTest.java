package org.swa.conf.business.service;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.swa.conf.business.events.PasswordExpiredEvent;
import org.swa.conf.configuration.EnvironmentEntriesHolder;
import org.swa.conf.datatypes.User;

@RunWith(Arquillian.class)
public class EventToQueueMediatorTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ArchiveProducer.createTestWebArchive();
		war.addClass(PasswordExpiredEvent.class);
		war.addClass(EventToQueueMediator.class);
		war.addClass(EventToQueueMediatorTest.class);
		war.addAsWebInfResource("mediator-ejb-jar.xml", "ejb-jar.xml");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private Logger											log;

	@Inject
	private Event<PasswordExpiredEvent>	event;

	@Inject
	private JMSContext									ctx;

	@Resource(lookup = "java:comp/env/MyTestQueue")
	private Queue												queue;

	@Inject
	private EnvironmentEntriesHolder		props;

	@Test
	public void delayedWindowedQueueTest() throws InterruptedException {

		final long start = System.currentTimeMillis();

		for (int i = 0; i < 30; i++) {
			final User u = new User();
			u.setId(Long.valueOf(i));
			u.setName("Name" + 1);
			u.setPassword("Not null");
			u.setLastPasswordChange(new Date(0));
			log.debug("Emitted user {} in {}", u, this);
			event.fire(new PasswordExpiredEvent(u));
		}

		final JMSConsumer c = ctx.createConsumer(queue);

		// wait for first message
		log.debug("Waiting the first message become deliverable (due configured delay) ...");
		final User firstUser = c.receiveBody(User.class);
		final long firstMessage = System.currentTimeMillis();
		log.debug("Got first message after {} [ms] ...", (firstMessage - start));

		assertNull(firstUser.getPassword());

		long expected = props.getLong("expiredPwdReportToReminderDelay");
		assertTrue("Expected delay " + expected + " was " + (firstMessage - start), firstMessage - start > expected);

		for (int i = 1; i < 29; i++)
			assertNull(c.receiveBody(User.class).getPassword()); // IFF no user received => NPE

		// let the last message expire
		log.debug("Wait the last message has expired ...");
		expected = props.getLong("expiredPwdReminderSendingWindow");
		TimeUnit.MILLISECONDS.sleep(expected + 200l);

		// there must not be any message left
		assertNull(c.receiveNoWait());
		log.debug("Yep ...");
	}
}