package org.swa.conf.mongo.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import com.github.fakemongo.Fongo;
import com.mongodb.Mongo;
import org.slf4j.Logger;

@Specializes
@ApplicationScoped
public class FongoDbProducer extends MongoDbProducer {

	@Inject
	private Logger log;

	@Override
	@Produces
	@javax.inject.Singleton
	Mongo getMongoClient() {
		log.debug("Getting a database using conection {}", getConnectionString());
		return new Fongo("FONGO: " + getConnectionString()).getMongo();
	}
}