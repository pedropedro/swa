package org.swa.conf.business.service;

import java.util.Date;
import javax.annotation.Resource;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.swa.conf.datatypes.User;

@MessageDriven
public class MailService implements javax.jms.MessageListener {

	@Inject
	private Logger log;

	@Resource
	private MessageDrivenContext mdCtx;

	/** Configured in ejb-jar.xml */
	private javax.mail.Session mailSession;

	@Override
	public void onMessage(final javax.jms.Message m) {

		log.debug("Received message {} in {}", m, this);

		try {
			// User with expired password
			final User user = m.getBody(User.class);
			log.debug("... containing {}", user);

			final MimeMessage eMail = new MimeMessage(mailSession);
			eMail.setFrom(InternetAddress.parse("swa-admin@swa.org", false)[0]);
			eMail.setRecipients(
					RecipientType.TO,
					InternetAddress.parse(
							user.getEmail() == null || user.getEmail().isEmpty() ? "test@test.org" : user.getEmail(),
							false));
			eMail.setSubject("Your password has expired");
			eMail.setHeader("X-Mailer", "JavaMail");
			eMail.setText("TEST");
			eMail.setSentDate(new Date());
			Transport.send(eMail);

			log.debug("Mail sent to {}", eMail.getRecipients(RecipientType.TO)[0]);

		} catch (final MessagingException | JMSException e) {

			log.warn("Error in sending e-mail: {}", e.getMessage());

			try {
				if (m.getJMSRedelivered() == false)
					mdCtx.setRollbackOnly();
				else
					log.warn("Tried again to process message");

			} catch (IllegalStateException | JMSException e2) {
				log.warn("Cannot rollback message consumption: {}", e2.getMessage());
			}
		}
	}
}