package org.swa.conf.mongo.services;

import javax.inject.Inject;

import static org.junit.Assert.*;

import com.mongodb.DBObject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.Speaker;
import org.swa.conf.mongo.DataLoader;
import org.swa.conf.mongo.collections.SpeakerCollection;
import org.swa.conf.mongo.producers.ArchiveProducer;

@RunWith(Arquillian.class)
public class SpeakerMassTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final Archive<WebArchive> a = ArchiveProducer.createMongoTestWebArchive();
		final Package p = SpeakerMassTest.class.getPackage();
		((WebArchive) a).addAsResource(p, "SpeakerMassTest#test1.json").addAsResource(
				p, "SpeakerMassTest#test1-expected.json");
		return a;
	}

	@Inject
	private DataLoader l;

	@Inject
	private BasePersistenceService<Speaker> persistence;

	@Inject
	private SpeakerPersistenceLocalBean localBeanDbReader;

	@Inject
	private Logger log;

	private static final String SPEAKERS = "speaker";

	@Test
	@InSequence(value = 10)
	public void test1() {

		l.load(SPEAKERS, DataLoader.Strategy.SET, "test1");

		final SpeakerCollection s = new SpeakerCollection();
		s.setId(40000000000L);
		s.setDescription("Speaker description");
		s.setName("Speaker 1");

		persistence.save(s);
		assertNotNull(s.getId());

		assertTrue(l.match(SPEAKERS, "test1"));

		l.dump("speaker", "test1");
	}

	@Test
	@InSequence(value = 20)
	public void test2() {
		log.debug("Current documents in the DB:");
		for (final DBObject dbo : localBeanDbReader.getCollection().getDBCollection().find())
			log.debug("{}", dbo);

		l.load(SPEAKERS, DataLoader.Strategy.TRIM, null);
	}

	@Test
	@InSequence(value = 30)
	public void test3() {
		l.load(SPEAKERS, DataLoader.Strategy.ADD, "test1");
	}

	@Test
	@InSequence(value = 40)
	public void test4() {
		l.trim(SPEAKERS);
		assertEquals(0, localBeanDbReader.getCollection().getDBCollection().find().length());
	}
}