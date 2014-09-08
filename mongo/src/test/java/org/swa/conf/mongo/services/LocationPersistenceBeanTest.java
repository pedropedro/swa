package org.swa.conf.mongo.services;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.AbstractDatatype;
import org.swa.conf.datatypes.Location;
import org.swa.conf.mongo.collections.LocationCollection;
import org.swa.conf.mongo.collections.RoomCollection;
import org.swa.conf.mongo.producers.ArchiveProducer;

@RunWith(Arquillian.class)
public class LocationPersistenceBeanTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createMongoTestWebArchive();
	}

	@Inject
	private BasePersistenceService<Location> persistence;

	@Test
	@InSequence(value = 10)
	public void notFound() {
		final Location rec = persistence.findById(-1L);
		Assert.assertNull(rec);
	}

	@Test
	@InSequence(value = 20)
	public void crud() {

		final List<RoomCollection> rooms = new ArrayList<>();
		RoomCollection room = new RoomCollection();
		room.setId(1L);
		room.setName("R1");
		room.setCapacity(100);
		rooms.add(room);
		room = new RoomCollection();
		room.setId(2L);
		room.setName("R22");
		room.setCapacity(500);
		rooms.add(room);

		final LocationCollection l = new LocationCollection();
		l.setId(null);
		l.setCity("Sin");
		l.setLatitude(66.66);
		l.setLongitude(6.6);
		l.setStreet("Abc Str");
		l.setRooms(rooms);

		for (final AbstractDatatype r : l.getRooms())
			Assert.assertNotNull(r.getId());

		// Create
		persistence.save(l);
		Assert.assertNotNull(l.getId());

		// Read
		final Location l2 = persistence.findById(l.getId());
		Assert.assertNotNull(l2);
		Assert.assertEquals(l, l2);

		// Update
		l.setLatitude(200.0);
		persistence.save(l);

		final Location l3 = persistence.findById(l.getId());
		Assert.assertNotNull(l3);
		Assert.assertEquals(l, l3);
		Assert.assertNotEquals(l2, l3);

		// Delete
		persistence.remove(l);
		final Location l4 = persistence.findById(l.getId());
		Assert.assertNull(l4);
	}
}