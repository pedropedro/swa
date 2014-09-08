package org.swa.conf.configuration;

import java.lang.management.ManagementFactory;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;

@ApplicationScoped
@LocalBean
public class RuntimeConfiguration implements RuntimeConfigurationMXBean {

	public static final ObjectName MBEAN_NAME;

	static {
		ObjectName on = null;
		try {
			on = new ObjectName("org.swa.conf:type=" + RuntimeConfiguration.class.getName());
		} catch (final MalformedObjectNameException e) {
			e.printStackTrace();
		} finally {
			MBEAN_NAME = on;
		}
	}

	private MBeanServer platformMBeanServer;

	@Inject
	private Logger log;

	@Inject
	private EnvironmentEntriesHolder entryHolder;

	@PostConstruct
	public void registerInJMX() {
		try {
			log.debug("Registering {} in JMX", RuntimeConfiguration.MBEAN_NAME);
			platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
			platformMBeanServer.registerMBean(this, RuntimeConfiguration.MBEAN_NAME);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@PreDestroy
	public void unregisterFromJMX() {
		log.debug("Unregistering {} from JMX", RuntimeConfiguration.MBEAN_NAME);
		try {
			platformMBeanServer.unregisterMBean(RuntimeConfiguration.MBEAN_NAME);
		} catch (final Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void setPasswordChangePeriodInDays(final Integer days) {
		entryHolder.setNewRuntimeValue(entryHolder.ENV_passwordChangePeriodInDays, days);
	}

	@Override
	public Integer getPasswordChangePeriodInDays() {
		return entryHolder.getInteger(entryHolder.ENV_passwordChangePeriodInDays);
	}
}