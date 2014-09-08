package org.swa.conf.monitoring;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.configuration.EnvironmentEntriesHolder;
import org.swa.conf.configuration.RuntimeConfiguration;
import org.swa.conf.configuration.RuntimeConfigurationMXBean;

@RunWith(Arquillian.class)
public class RuntimeConfigurationTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ShrinkWrap.create(WebArchive.class, "management-ejb.war");
		war.addPackage(EnvironmentEntriesHolder.class.getPackage());
		war.addClass(LoggerProducer.class);
		war.addAsWebInfResource("empty-beans.xml", "beans.xml");
		war.addAsWebInfResource("ejb-jar.xml");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private RuntimeConfigurationMXBean config;

	@Inject
	private EnvironmentEntriesHolder eh;

	@Test
	@InSequence(value = 10)
	public void configPasswordChangePeriodInDays() {

		assertEquals(Integer.valueOf(90), config.getPasswordChangePeriodInDays());

		config.setPasswordChangePeriodInDays(Integer.valueOf(100));

		assertEquals(Integer.valueOf(100), config.getPasswordChangePeriodInDays());
	}

	@Test(expected = EJBException.class)
	@InSequence(value = 20)
	public void configUnknownEntry() {

		final String unknownName = "dummy";

		try {
			eh.getInteger(unknownName);
		} catch (final Exception e) {
			assertEquals("Unknown environment entry '" + unknownName + "' !", e.getMessage());
			throw e;
		}
	}

	@Test(expected = EJBException.class)
	@InSequence(value = 21)
	public void configDifferentTypes() {
		try {
			eh.setNewRuntimeValue(eh.ENV_passwordChangePeriodInDays, "dummy");
		} catch (final Exception e) {
			assertEquals("Old and new value have different types !", e.getMessage());
			throw e;
		}
	}

	@Test
	@InSequence(value = 30)
	@RunAsClient
	public void configExternalPasswordChangePeriodInDays() throws Exception {

		// Get a connection to the WildFly 8 MBean server on localhost
		final String urlString = System.getProperty("jmx.service.url",
				"service:jmx:http-remoting-jmx://localhost:9990");

		try (
				JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(urlString), null)
		) {
			final MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

			final RuntimeConfigurationMXBean mbean = JMX.newMXBeanProxy(connection, RuntimeConfiguration.MBEAN_NAME,
					RuntimeConfigurationMXBean.class);

			// not the start-up default (90) - we have changed it in the first test !
			assertEquals(Integer.valueOf(100), mbean.getPasswordChangePeriodInDays());

			mbean.setPasswordChangePeriodInDays(Integer.valueOf(200));

			assertEquals(Integer.valueOf(200), mbean.getPasswordChangePeriodInDays());
		}
	}
}