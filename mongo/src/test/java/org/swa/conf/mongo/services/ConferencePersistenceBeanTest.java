package org.swa.conf.mongo.services;

import java.util.ArrayList;
import java.util.Date;
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
import org.swa.conf.datatypes.Conference;
import org.swa.conf.mongo.collections.ConferenceCollection;
import org.swa.conf.mongo.collections.LocationCollection;
import org.swa.conf.mongo.collections.RoomCollection;
import org.swa.conf.mongo.collections.SpeakerCollection;
import org.swa.conf.mongo.collections.TalkCollection;
import org.swa.conf.mongo.producers.ArchiveProducer;

@RunWith(Arquillian.class)
public class ConferencePersistenceBeanTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createMongoTestWebArchive();
	}

	@Inject
	private BasePersistenceService<Conference> persistence;

	@Test
	@InSequence(value = 10)
	public void crud() {

		final List<RoomCollection> rooms = new ArrayList<>();
		RoomCollection r = new RoomCollection();
		r.setId(1L);
		r.setName("R1");
		r.setCapacity(100);
		rooms.add(r);
		r = new RoomCollection();
		r.setId(2L);
		r.setName("R22");
		r.setCapacity(500);
		rooms.add(r);

		final List<SpeakerCollection> speakers = new ArrayList<>();
		final SpeakerCollection s = new SpeakerCollection();
		s.setId(1L);
		s.setDescription("Speaker description");
		s.setName("Speaker 1");
		speakers.add(s);

		final List<TalkCollection> talks = new ArrayList<>();
		final TalkCollection t = new TalkCollection();
		t.setId(1L);
		t.setFrom(new Date());
		t.setName("Talk name");
		t.setRooms(rooms);
		t.setShortAbstract("Talk short abstract");
		t.setSpeakers(speakers);
		t.setTo(new Date());
		talks.add(t);

		final LocationCollection l = new LocationCollection();
		l.setId(1L);
		l.setCity("Sin");
		l.setLatitude(66.66);
		l.setLongitude(6.6);
		l.setStreet("Abc Str");
		l.setRooms(rooms);

		final ConferenceCollection c = new ConferenceCollection();
		c.setId(null);
		c.setCity(l);
		c.setDescription("Conference description");
		c.setFrom(new Date());
		c.setName("Conference name");
		c.setTalks(talks);
		c.setTo(new Date());

		Assert.assertNotNull(c.getCity().getId());
		for (final org.swa.conf.datatypes.Talk tt : c.getTalks()) {
			Assert.assertNotNull(tt.getId());
			for (final AbstractDatatype ad : tt.getRooms())
				Assert.assertNotNull(ad.getId());
			for (final AbstractDatatype ad : tt.getSpeakers())
				Assert.assertNotNull(ad.getId());
		}

		// Create
		persistence.save(c);
		Assert.assertNotNull(c.getId());

		// Read
		final Conference c2 = persistence.findById(c.getId());
		Assert.assertNotNull(c2);
		Assert.assertEquals(c, c2);

		// Update
		c.setTo(new Date(0L));
		persistence.save(c);

		final Conference c3 = persistence.findById(c.getId());
		Assert.assertNotNull(c3);
		Assert.assertEquals(c, c3);
		Assert.assertNotEquals(c2, c3);

		// Delete
		persistence.remove(c);
		final Conference c4 = persistence.findById(c.getId());
		Assert.assertNull(c4);
	}
}