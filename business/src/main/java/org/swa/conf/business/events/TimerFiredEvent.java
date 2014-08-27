package org.swa.conf.business.events;

import java.io.Serializable;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.NoMoreTimeoutsException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

public class TimerFiredEvent {

	private final TimerHandle	handle;

	public TimerFiredEvent(final TimerHandle handle) {

		this.handle = handle != null ? handle : new /* dummy */TimerHandle() {

			private static final long	serialVersionUID	= 1L;

			@Override
			public Timer /* dummy */getTimer() throws IllegalStateException, NoSuchObjectLocalException, EJBException {

				return new Timer() {

					@Override
					public boolean isPersistent() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
						return false;
					}

					@Override
					public boolean isCalendarTimer() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
						return false;
					}

					@Override
					public long getTimeRemaining() throws IllegalStateException, NoSuchObjectLocalException,
							NoMoreTimeoutsException, EJBException {
						return 0;
					}

					@Override
					public ScheduleExpression getSchedule() throws IllegalStateException, NoSuchObjectLocalException,
							EJBException {
						return null;
					}

					@Override
					public Date getNextTimeout() throws IllegalStateException, NoSuchObjectLocalException,
							NoMoreTimeoutsException, EJBException {
						return null;
					}

					@Override
					public Serializable getInfo() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
						return null;
					}

					@Override
					public TimerHandle getHandle() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
						return null;
					}

					@Override
					public void cancel() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
					}
				};
			}
		};
	}

	public TimerHandle getTimerHandle() {
		return handle;
	}
}