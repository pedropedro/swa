package org.swa.conf.mongo.services;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.Speaker;
import org.swa.conf.mongo.collections.SpeakerCollection;
import org.swa.conf.mongo.producers.ArchiveProducer;

@RunWith(Arquillian.class)
public class SpeakerPersistenceBeanTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createMongoTestWebArchive();
	}

	@Inject
	private BasePersistenceService<Speaker>	persistence;

	@Test
	@InSequence(value = 10)
	public void notFound() {
		final Speaker rec = persistence.findById(new ObjectId("1234567890abcdef12345678"));
		Assert.assertNull(rec);
	}

	@Test
	@InSequence(value = 20)
	public void crud() {

		final Speaker s = new SpeakerCollection().withOid();
		s.setDescription("Speaker description");
		s.setName("Speaker 1");

		Assert.assertNotNull(s.getId());

		// Create
		persistence.save(s);

		// Read
		final Speaker s2 = persistence.findById(s.getId());
		Assert.assertNotNull(s2);
		Assert.assertEquals(s, s2);

		// Update
		s.setName("Speaker 2");
		persistence.save(s);

		final Speaker s3 = persistence.findById(s.getId());
		Assert.assertNotNull(s3);
		Assert.assertEquals(s, s3);
		Assert.assertNotEquals(s2, s3);

		// Delete
		persistence.remove(s);
		final Speaker s4 = persistence.findById(s.getId());
		Assert.assertNull(s4);
	}
}