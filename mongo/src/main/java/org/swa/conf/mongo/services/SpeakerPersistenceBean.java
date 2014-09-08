package org.swa.conf.mongo.services;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jongo.MongoCollection;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.Speaker;
import org.swa.conf.mongo.annotations.NamedCollection;
import org.swa.conf.mongo.collections.SpeakerCollection;

/** Stateless although internal state - the injected {@link MongoCollection} is always a new instance */
@Stateless
@Local(BasePersistenceService.class)
public class SpeakerPersistenceBean extends BasePersistenceBean<Speaker> {

	@Inject
	@NamedCollection(logicalName = "speaker")
	private MongoCollection c;

	@Override
	public MongoCollection getCollection() {
		log.debug("Instance {}", this);
		return c;
	}

	@Override
	@PostConstruct
	protected void setGenericClass() {
		setGenericClass(SpeakerCollection.class);
	}
}