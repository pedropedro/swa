package org.swa.conf.mongo.producers;

import java.net.UnknownHostException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.inject.Inject;

import com.mongodb.Mongo;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import org.slf4j.Logger;
import org.swa.conf.configuration.EnvironmentEntriesHolder;

@ApplicationScoped
class MongoDbProducer {

	@Inject
	private Logger log;

	@Inject
	private EnvironmentEntriesHolder entryHolder;

	void close(@Disposes final Mongo mongoClient) {
		log.debug("Closing {}", mongoClient);
		mongoClient.close();
	}

	/**
	 * @param bs
	 */
	void freeResources(@Observes final BeforeShutdown bs) {
		log.debug("Bye, bye !");
	}

	String getConnectionString() {
		return entryHolder.getString("connectionStringMongo");
	}

	@Produces
	@javax.inject.Singleton
	Mongo getMongoClient() throws UnknownHostException {
		log.debug("Getting a database using conection {}", getConnectionString());

		final MongoClientOptions.Builder options = new MongoClientOptions.Builder();
		options.cursorFinalizerEnabled(false);

		final MongoClientURI mongoURI = new MongoClientURI(getConnectionString(), options);

		return Mongo.Holder.singleton().connect(mongoURI);
	}
}