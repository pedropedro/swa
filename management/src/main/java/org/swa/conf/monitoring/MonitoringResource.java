package org.swa.conf.monitoring;

import java.lang.management.ManagementFactory;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;

@ApplicationScoped
@LocalBean
public class MonitoringResource implements MonitoringResourceMXBean {

	public static final ObjectName MBEAN_NAME;

	static {
		ObjectName on = null;
		try {
			on = new ObjectName("org.swa.conf:type=" + MonitoringResource.class.getName());
		} catch (final MalformedObjectNameException e) {
			e.printStackTrace();
		} finally {
			MBEAN_NAME = on;
		}
	}

	private MBeanServer platformMBeanServer;

	@Inject
	private StatisticsPersister persister;

	@Inject
	private Logger log;

	@PostConstruct
	public void registerInJMX() {
		try {
			log.debug("Registering {} in JMX", MBEAN_NAME);
			platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
			platformMBeanServer.registerMBean(this, MBEAN_NAME);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@PreDestroy
	public void unregisterFromJMX() {
		log.debug("Unregistering {} from JMX", MBEAN_NAME);
		try {
			platformMBeanServer.unregisterMBean(MBEAN_NAME);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}

	// MXBean -----------------------------------------------------------
	@Override
	public StatisticsPersister.HistogramEntry[] getHistogram(final String statType, final String key, final Date from,
			final Date to, final String coarseness) {

		final StatisticsPersister.StatType st;

		if (statType.toLowerCase().startsWith("i"))
			st = StatisticsPersister.StatType.INVOCATIONS;
		else if (statType.toLowerCase().startsWith("e"))
			st = StatisticsPersister.StatType.EXCEPTIONS;
		else if (statType.toLowerCase().startsWith("r"))
			st = StatisticsPersister.StatType.RESPONSES;
		else
			throw new IllegalArgumentException("Supported values for P1: InVoCaTions, EXCEPTIONS, responses");

		log.debug("Getting histogram for {} / '{}'", st, key);

		return persister.getStatistics(st, key == null || key.isEmpty() ? ".*" : key, from, to, coarseness);
	}

	/** Just for local clients ( JUnit tests ? ) */
	public void resetStats() {
		persister.resetStatistics();
	}

	// Bridge to Interceptors ------------------------------------------------

	/** Convenience for interceptors */
	public String constructKey(final InvocationContext ctx) {
		return ctx.getTarget().getClass().getName() + ctx.getMethod().getName();
	}

	public void exceptionThrown(final String key) {
		persister.addExceptionCount(key);
	}

	public void methodInvoked(final String key) {
		persister.addInvocationCount(key);
	}

	public void methodResponseTime(final String key, final long millis) {
		persister.addResponseTime(key, millis);
	}
}