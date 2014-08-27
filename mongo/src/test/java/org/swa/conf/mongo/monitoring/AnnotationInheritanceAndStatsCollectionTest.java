package org.swa.conf.mongo.monitoring;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.configuration.EnvironmentEntriesHolder;
import org.swa.conf.datatypes.User;
import org.swa.conf.mongo.producers.ArchiveProducer;
import org.swa.conf.monitoring.MonitoringResource;
import org.swa.conf.monitoring.StatisticsPersister.HistogramEntry;
import org.swa.conf.monitoring.StatisticsPersister.StatType;

@RunWith(Arquillian.class)
public class AnnotationInheritanceAndStatsCollectionTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ShrinkWrap.create(WebArchive.class, "conf-ejb.war");
		war.addPackages(true, "org.swa.conf.mongo");
		war.addPackages(false, "org.swa.conf.datatypes");
		war.addPackages(false, "org.swa.conf.business.persistence");
		war.addPackages(false, "org.swa.conf.monitoring");
		war.addClass(EnvironmentEntriesHolder.class);
		war.addAsLibraries(ArchiveProducer.pers.resolve("org.mongodb:mongo-java-driver").withTransitivity().asFile());
		war.addAsLibraries(ArchiveProducer.pers.resolve("org.jongo:jongo").withTransitivity().asFile());
		war.addAsLibraries(ArchiveProducer.pers.resolve("com.github.fakemongo:fongo").withTransitivity().asFile());
		war.addAsWebInfResource("mongo-ejb-jar.xml", "ejb-jar.xml");
		war.addAsWebInfResource("interceptor-beans.xml", "beans.xml");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private Logger												log;

	@Inject
	private MonitoringResource						monitor;

	@Inject
	private BasePersistenceService<User>	userService;

	@Test
	@InSequence(value = 10)
	public void notFound() {

		final int loops = 50;

		for (int i = 0; i < loops; i++) {
			final User rec = userService.findById(new ObjectId("1234567890abcdef12345678"));
			assertNull(rec);
		}

		HistogramEntry[] histogram = monitor.getHistogram(StatType.EXCEPTIONS.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		assertEquals(Long.valueOf(0), histogram[0].getMeasure());

		// If @Inherited in @StatisticsSource didn't work, we'd get here 0 (no interceptor bound occurred)
		histogram = monitor.getHistogram(StatType.INVOCATIONS.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		assertEquals(Long.valueOf(loops), histogram[0].getMeasure());

		histogram = monitor.getHistogram(StatType.RESPONSES.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		log.debug("Response time: {} ms", histogram[0].getMeasure());
		assertTrue(histogram[0].getMeasure() >= loops);
	}
}