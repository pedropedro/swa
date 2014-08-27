package org.swa.conf.business.service;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.Timer;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.swa.conf.business.events.PasswordExpiredEvent;
import org.swa.conf.business.events.TimerFiredEvent;
import org.swa.conf.business.events.TimerId;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.configuration.EnvironmentEntriesHolder;
import org.swa.conf.datatypes.User;

@Stateless
public class MaintenanceService {

	private final long										DAY_IN_MILLIS	= 24l * 60l * 60l * 1000l;

	@Inject
	private Logger												log;

	@Inject
	private BasePersistenceService<User>	persistence;

	@Inject
	private Event<PasswordExpiredEvent>		event;

	@Inject
	@TimerId("generateReportForExpiredPasswords")
	private Event<TimerFiredEvent>				timerEvent;

	@Inject
	private EnvironmentEntriesHolder			props;

	@Schedule(hour = "03", minute = "16")
	public void generateReportForExpiredPasswords(final Timer t) {

		// demonstrate how to publish from a stateless EJB a timer that can be cancelled
		// (ok, for automatic timers rather useless ;-)
		timerEvent.fire(new TimerFiredEvent(t.isPersistent() ? t.getHandle() : null));

		for (final User u : persistence.findAll()) {
			if (hasExpiredPassword(u)) {
				final PasswordExpiredEvent e = new PasswordExpiredEvent(u);
				log.debug("{} should change their password", e.getUser()); // log w/o password
				event.fire(e);
			}
		}
	}

	public boolean hasExpiredPassword(final User u) {
		return u.getLastPasswordChange() == null
				|| System.currentTimeMillis() - u.getLastPasswordChange().getTime() > DAY_IN_MILLIS
						* props.getInteger(props.ENV_passwordChangePeriodInDays).longValue();
	}
}