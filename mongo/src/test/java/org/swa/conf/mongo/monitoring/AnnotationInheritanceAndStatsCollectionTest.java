package org.swa.conf.mongo.monitoring;

import javax.inject.Inject;

import static org.junit.Assert.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.User;
import org.swa.conf.mongo.producers.ArchiveProducer;
import org.swa.conf.monitoring.MonitoringResource;
import org.swa.conf.monitoring.StatisticsPersister;

@RunWith(Arquillian.class)
public class AnnotationInheritanceAndStatsCollectionTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ArchiveProducer.createMongoTestWebArchive();
		war.addPackages(false, "org.swa.conf.monitoring");
		war.addAsWebInfResource("interceptor-beans.xml", "beans.xml");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private Logger log;

	@Inject
	private MonitoringResource monitor;

	@Inject
	private BasePersistenceService<User> userService;

	@Test
	@InSequence(value = 10)
	public void notFound() {

		final int loops = 50;

		for (int i = 0; i < loops; i++) {
			final User rec = userService.findById(-1L);
			assertNull(rec);
		}

		StatisticsPersister.HistogramEntry[] histogram = monitor.getHistogram(StatisticsPersister.StatType.EXCEPTIONS
				.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		assertEquals(Long.valueOf(0), histogram[0].getMeasure());

		// If @Inherited in @StatisticsSource didn't work, we'd get here 0 (no interceptor bound occurred)
		histogram = monitor.getHistogram(StatisticsPersister.StatType.INVOCATIONS.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		assertEquals(Long.valueOf(loops), histogram[0].getMeasure());

		histogram = monitor.getHistogram(StatisticsPersister.StatType.RESPONSES.name(), ".*", null, null, "1H");
		assertEquals(1, histogram.length);
		log.debug("Response time: {} ms", histogram[0].getMeasure());
		assertTrue(histogram[0].getMeasure() >= loops);
	}
}