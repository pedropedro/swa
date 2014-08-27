package org.swa.conf.business.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.swa.conf.datatypes.AbstractDatatype;
import org.swa.conf.datatypes.User;
import org.swa.conf.monitoring.LoggerProducer;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(Arquillian.class)
public class MailServiceTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ShrinkWrap.create(WebArchive.class, "business-ejb.war");
		war.addPackage(AbstractDatatype.class.getPackage());
		war.addClass(MailService.class);
		war.addClass(MailServiceTest.class);
		war.addClass(LoggerProducer.class);
		war.addAsWebInfResource("empty-beans.xml", "beans.xml");
		war.addAsWebInfResource("mail-ejb-jar.xml", "ejb-jar.xml");
		war.addAsLibraries(ArchiveProducer.pers.resolve("com.icegreen:greenmail").withoutTransitivity().asFile());
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private Logger			log;

	@Inject
	private JMSContext	ctx;

	@Resource(lookup = "java:comp/env/MyTestQueue")
	private Queue				queue;

	@Test
	public void sendMailTest() throws InterruptedException, IOException, MessagingException {

		final JMSProducer p = ctx.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).setTimeToLive(10000);

		GreenMail gm = null;

		try {
			gm = new GreenMail(new ServerSetup(25, "localhost", "smtp"));
			gm.start();

			final int userCount = 5;

			// push some Users to the queue
			for (int i = 0; i < userCount; i++) {
				final User u = new User();
				u.setId(Long.valueOf(i));
				u.setName("Name" + 1);
				u.setEmail("u" + i + "@test.org");
				log.debug("Emitted user {} in {}", u, this);
				p.send(queue, u);
			}

			// wait to all mails but at most 5 seconds
			gm.waitForIncomingEmail(TimeUnit.SECONDS.toMillis(5), userCount);

			final MimeMessage[] receivedMessages = gm.getReceivedMessages();
			assertEquals(userCount, receivedMessages.length);

			for (final MimeMessage m : receivedMessages) {
				assertTrue(m.getRecipients(RecipientType.TO).length > 0); // bug in GreenMail ? doppeltes TO
				assertTrue(m.getRecipients(RecipientType.TO)[0].toString().matches("u[0-9]*@test\\.org"));
				assertEquals("Your password has expired", m.getSubject());
				assertEquals("TEST\r\n", m.getContent()); // \r\n: bug in GreenMail oder MIME standard ?
			}

		} finally {
			if (gm != null)
				gm.stop();
		}
	}
}