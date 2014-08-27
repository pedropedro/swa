package org.swa.conf.monitoring;

import static org.junit.Assert.*;

import java.util.Properties;
import java.util.Random;

import javax.inject.Inject;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.swa.conf.configuration.EnvironmentEntriesHolder;
import org.swa.conf.monitoring.StatisticsPersister.HistogramEntry;
import org.swa.conf.monitoring.StatisticsPersister.StatType;

@RunWith(Arquillian.class)
public class MonitoringResourceTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ShrinkWrap.create(WebArchive.class, "management-ejb.war");
		war.addPackage(MonitoringResource.class.getPackage());
		war.addClass(EnvironmentEntriesHolder.class);
		war.addAsWebInfResource("interceptor-beans.xml", "beans.xml");
		war.addAsWebInfResource("ejb-jar.xml");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private MonitoringResource	monitor;

	@Inject
	MonitoredBean								target;

	@Inject
	private Logger							log;

	@Test
	@InSequence(value = 10)
	public void getHistogram() {

		final int loops = 500;
		final Random r = new Random(0);

		for (int i = 0; i < loops; i++)
			target.test(r); // in-container == call by reference !

		long exCnt = 0;
		long invCnt = 1;
		long resp = 0;

		HistogramEntry[] histogram = monitor.getHistogram(StatType.EXCEPTIONS.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		assertEquals(Long.valueOf(loops / 5), Long.valueOf(exCnt = histogram[0].getMeasure()));
		log.info("Exception count: {}", exCnt);

		histogram = monitor.getHistogram(StatType.INVOCATIONS.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		assertEquals(Long.valueOf(loops), Long.valueOf(invCnt = histogram[0].getMeasure()));
		log.info("Invocation count: {}", invCnt);

		histogram = monitor.getHistogram(StatType.RESPONSES.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		// shortest *measured* method call duration is 1ms in my system !
		assertTrue((resp = histogram[0].getMeasure()) > loops);
		log.info("Total response time: {} [ms]", resp);

		log.info("Average response time including exceptions: {}", resp / invCnt);
		log.info("Average response time excluding exceptions: {}", (resp - exCnt) / invCnt);

		monitor.resetStats();
		histogram = monitor.getHistogram(StatType.EXCEPTIONS.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		assertEquals(Long.valueOf(0), Long.valueOf(histogram[0].getMeasure()));
		histogram = monitor.getHistogram(StatType.INVOCATIONS.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		assertEquals(Long.valueOf(0), Long.valueOf(histogram[0].getMeasure()));
		histogram = monitor.getHistogram(StatType.RESPONSES.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		assertEquals(Long.valueOf(0), Long.valueOf(histogram[0].getMeasure()));
	}

	@Test
	@InSequence(value = 20)
	@RunAsClient
	public void getHistogramRemote(@ArquillianResource final InitialContext context) throws Exception {

		// out of the EJB / CDI container the injected "log" is not available !
		final java.util.logging.Logger clientLog = java.util.logging.Logger.getLogger(this.getClass().getSimpleName());

		// client remoting properties
		final Properties c = new Properties();
		c.put("remote.connections", "default");
		c.put("remote.connection.default.host", "localhost");
		c.put("remote.connection.default.port", "8080");
		// For non localhost containers
		// c.put("remote.connection.default.username", "username");
		// c.put("remote.connection.default.password", "secretPassword");
		// c.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
		// c.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");

		EJBClientContext.setSelector(new ConfigBasedEJBClientContextSelector(new PropertiesBasedEJBClientConfiguration(c)));

		// ### Instead of old fashion style of creating InitialContext, let it get injected into the test ...
		// final Properties p = new Properties();
		// p.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		// final Context context = new InitialContext(p);
		final MonitoredService remoteTarget = (MonitoredService) context
				.lookup("ejb:/management-ejb/MonitoredBean!org.swa.conf.monitoring.MonitoredService");

		final int loops = 500;
		Random r = new Random(0);

		for (int i = 0; i < loops; i++)
			r = remoteTarget.test(r); // out of container == call by value !

		long exCnt = 0;
		long invCnt = 1;
		long resp = 0;

		// Get a connection to the WildFly 8 MBean server on localhost
		final String urlString = System.getProperty("jmx.service.url", "service:jmx:http-remoting-jmx://localhost:9990");

		try (final JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(urlString), null))

		{
			final MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

			final MonitoringResourceMXBean mbean = JMX.newMXBeanProxy(connection, MonitoringResource.MBEAN_NAME,
					MonitoringResourceMXBean.class);

			HistogramEntry[] histogram = mbean.getHistogram(StatType.EXCEPTIONS.name(), ".*", null, null, "1H");
			assertEquals(1, histogram.length);
			assertEquals(Long.valueOf(loops / 5), Long.valueOf(exCnt = histogram[0].getMeasure()));
			clientLog.info("Exception count: " + exCnt);

			histogram = mbean.getHistogram(StatType.INVOCATIONS.name(), ".*", null, null, "1H");
			assertEquals(1, histogram.length);
			assertEquals(Long.valueOf(loops), Long.valueOf(invCnt = histogram[0].getMeasure()));
			clientLog.info("Invocation count: " + invCnt);

			histogram = mbean.getHistogram(StatType.RESPONSES.name(), ".*", null, null, "1H");
			assertEquals(1, histogram.length);
			assertTrue((resp = histogram[0].getMeasure()) > 0l & resp < loops * 500l); // a method call must be under 500ms !?
			clientLog.info("Total response time: " + resp + " [ms]");

			clientLog.info("Average response time including exceptions: " + resp / invCnt);
			clientLog.info("Average response time excluding exceptions: " + (resp - exCnt) / invCnt);
		}
	}
}