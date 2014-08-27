package org.swa.conf.mongo.producers;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.swa.conf.configuration.EnvironmentEntriesHolder;
import org.swa.conf.mongo.annotations.NamedCollection;

import com.mongodb.Mongo;

@ApplicationScoped
class CollectionProducer {

	private static final String									COLL_PREFIX	= "collection.";

	@Inject
	private Logger															log;

	@Inject
	private EnvironmentEntriesHolder						entryHolder;

	private final ConcurrentMap<String, Jongo>	_jongos			= new ConcurrentHashMap<>();

	@Produces
	@Dependent
	@NamedCollection()
	private MongoCollection getCollection(final InjectionPoint ip, final Mongo mongo) {

		// retrieve NamedCollection
		String collectionLogicalName = null;
		for (final Annotation a : ip.getQualifiers()) {
			if (a.annotationType() == NamedCollection.class) {
				collectionLogicalName = ((NamedCollection) a).logicalName();
				log.debug("Found qualifier NamedCollection.class with collection ({})", collectionLogicalName);
				break;
			}
		}

		if (collectionLogicalName == null)
			throw new IllegalArgumentException("Missing qualifier @NamedCollection(<collectionName>)");
		// -----------------------------------------------------------

		// get physical names for the database and database collection
		final String key = CollectionProducer.COLL_PREFIX + collectionLogicalName;
		final String physicalName = entryHolder.getString(key);

		if (physicalName == null || physicalName.isEmpty())
			throw new IllegalArgumentException("Missing key or value for " + key + " in mongodb.properties");

		final int indexOfSeparator = physicalName.indexOf('.');
		if (physicalName.length() < 3 || indexOfSeparator < 1 || indexOfSeparator == physicalName.length() - 1)
			throw new IllegalArgumentException("Illegal value for " + key
					+ " in mongodb.properties. Expected <database_name>.<collection_name>");

		final String dbName = physicalName.substring(0, indexOfSeparator);
		final String collName = physicalName.substring(indexOfSeparator + 1);
		// -----------------------------------------------------------

		// cache new Jongo wrapper for every database
		Jongo jongo = _jongos.get(dbName);
		if (jongo == null) {
			final Jongo newbie = new Jongo(mongo.getDB(dbName));
			jongo = _jongos.putIfAbsent(dbName, newbie);
			log.debug("Cached new Jongo({})", dbName);
			if (jongo == null)
				jongo = newbie;
		}
		// -------------------------------------------

		final MongoCollection collection = jongo.getCollection(collName);
		log.debug("Found collection {}.{} ({})", dbName, collName, collectionLogicalName);

		return collection;
	}
}