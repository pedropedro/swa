package org.swa.conf.mongo.services;

import static org.junit.Assert.*;

import javax.inject.Inject;

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

import com.mongodb.DBObject;

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
	private DataLoader											l;

	@Inject
	private BasePersistenceService<Speaker>	persistence;

	@Inject
	private SpeakerPersistenceLocalBean			localBeanDbReader;

	@Inject
	private Logger													log;

	private static String										SPEAKERS	= "speaker";

	@Test
	@InSequence(value = 10)
	public void test1() {

		l.load(SpeakerMassTest.SPEAKERS, DataLoader.Strategy.SET, "test1");

		final SpeakerCollection s = new SpeakerCollection().withOid("000000000000000000000004");
		s.setDescription("Speaker description");
		s.setName("Speaker 1");

		assertNotNull(s.getId());

		persistence.save(s);

		assertTrue(l.match(SpeakerMassTest.SPEAKERS, "test1"));

		l.dump("speaker", "test1");
	}

	@Test
	@InSequence(value = 20)
	public void test2() {
		log.debug("Current documents in the DB:");
		for (final DBObject dbo : localBeanDbReader.getCollection().getDBCollection().find())
			log.debug("{}", dbo);

		l.load(SpeakerMassTest.SPEAKERS, DataLoader.Strategy.TRIM, null);
	}

	@Test
	@InSequence(value = 30)
	public void test3() {
		l.load(SpeakerMassTest.SPEAKERS, DataLoader.Strategy.ADD, "test1");
	}

	@Test
	@InSequence(value = 40)
	public void test4() {
		l.trim(SpeakerMassTest.SPEAKERS);
		assertEquals(0, localBeanDbReader.getCollection().getDBCollection().find().length());
	}
}