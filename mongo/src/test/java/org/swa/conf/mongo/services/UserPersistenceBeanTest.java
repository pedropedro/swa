package org.swa.conf.mongo.services;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.User;
import org.swa.conf.mongo.collections.UserCollection;
import org.swa.conf.mongo.producers.ArchiveProducer;

@RunWith(Arquillian.class)
public class UserPersistenceBeanTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createMongoTestWebArchive();
	}

	@Inject
	private BasePersistenceService<User>	persistence;

	@Test
	@InSequence(value = 10)
	public void notFound() {
		final User rec = persistence.findById(new ObjectId("1234567890abcdef12345678"));
		assertNull(rec);
	}

	@Test
	@InSequence(value = 20)
	public void crud() {

		final UserCollection s = new UserCollection().withOid();
		s.setPassword("password");
		s.setName("User 1");

		assertNotNull(s.getId());

		// Create
		persistence.save(s);

		// Read
		final User s2 = persistence.findById(s.getId());
		assertNotNull(s2);
		assertEquals(s, s2);

		// Update
		s.setName("Speaker 2");
		persistence.save(s);

		final User s3 = persistence.findById(s.getId());
		assertNotNull(s3);
		assertEquals(s, s3);
		assertNotEquals(s2, s3);

		// Delete
		persistence.remove(s);
		final User s4 = persistence.findById(s.getId());
		assertNull(s4);
	}
}