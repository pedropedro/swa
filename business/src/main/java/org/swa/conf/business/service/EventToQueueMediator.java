package org.swa.conf.business.service;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;

import org.slf4j.Logger;
import org.swa.conf.business.events.PasswordExpiredEvent;
import org.swa.conf.configuration.EnvironmentEntriesHolder;

@Stateless
public class EventToQueueMediator {

	@Inject
	private Logger										log;

	@Inject
	private JMSContext								ctx;

	/** Configured in ejb-jar.xml */
	private Queue											queue;

	@Inject
	private EnvironmentEntriesHolder	props;

	public void receiver(@Observes final PasswordExpiredEvent e) {

		if (queue == null)
			return;

		log.debug("Received user {} in {}", e.getUser(), this);

		// User with expired passwords will be reminded daily -> but please not 2 or more reminder clustered in case
		// our JMS system crashed in between => let the reminders expire when not sent until xxxH after they should have
		// been sent.
		final long startSendReminders = props.getLong("expiredPwdReportToReminderDelay");
		final long stopSendReminders = props.getLong("expiredPwdReminderSendingWindow");

		// persistent message per default
		ctx.createProducer().setDeliveryDelay(startSendReminders).setTimeToLive(startSendReminders + stopSendReminders)
		.setPriority(6).send(queue, e.getUser());
	}

	@PostConstruct
	private void init() {
		if (queue == null)
			log.warn("Missing configuration for expired passwords Queue in ejb-jar.xml !  NO MESSAGES WILL BE SEND");
	}
}