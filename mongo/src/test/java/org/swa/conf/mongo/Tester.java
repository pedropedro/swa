package org.swa.conf.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.fakemongo.Fongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.swa.conf.mongo.collections.ConferenceCollection;
import org.swa.conf.mongo.collections.LocationCollection;
import org.swa.conf.mongo.collections.RoomCollection;
import org.swa.conf.mongo.collections.SpeakerCollection;
import org.swa.conf.mongo.collections.TalkCollection;

public class Tester {

	private static final boolean useFake = true;

	public static void main(final String[] args) throws UnknownHostException {

		final String connString = "mongodb://localhost:27017";

		final MongoClientOptions.Builder options = new MongoClientOptions.Builder();
		options.cursorFinalizerEnabled(false);

		final MongoClientURI mongoURI = new MongoClientURI(connString, options);

		final Mongo mongo = Tester.useFake ? new Fongo("Fongo").getMongo() : Mongo.Holder.singleton().connect
				(mongoURI);

		final Jongo jongo = new Jongo(mongo.getDB("test"));
		final MongoCollection cl = jongo.getCollection("cl");
		final DB db = mongo.getDB("test");
		final DBCollection dbc = db.getCollection("cl");

		for (final DBObject dbo : dbc.find()) {
			System.out.println(dbo);
		}

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
		l.setName("Sin");
		l.setLatitude(66.66);
		l.setLongitude(6.6);
		l.setAddress("Abc Str");
		l.setRooms(rooms);

		final ConferenceCollection c = new ConferenceCollection();
		c.setId(1L);
		c.setLocation(l);
		c.setDescription("Conference description");
		c.setFrom(new Date());
		c.setName("Conference name");
		c.setTalks(talks);
		c.setTo(new Date());

		final WriteResult wr = cl.withWriteConcern(new WriteConcern(1, 0, false, true)).save(c);

		System.out.println(wr);
		System.out.println(c);

		System.out.println("---> DB state");
		for (final DBObject dbo : dbc.find())
			System.out.println(dbo);
		System.out.println("<--- DB state");

		final Long newId = c.getId();
		final ConferenceCollection locRead = cl.findOne("{_id:#}", newId).as(ConferenceCollection.class);

		System.out.println(locRead);

		for (final ConferenceCollection r1 : cl.find().as(ConferenceCollection.class))
			System.out.println(r1);

		final DBObject findOne = dbc.findOne(newId);
		System.out.println("## " + findOne);

		// ---------------------------------
		mongo.close();
	}
}