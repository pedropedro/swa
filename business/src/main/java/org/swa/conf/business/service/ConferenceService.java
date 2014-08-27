package org.swa.conf.business.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.Conference;

@Stateless
public class ConferenceService {

	@Inject
	private Logger															log;

	@Inject
	private BasePersistenceService<Conference>	persistence;

	public Conference findById(final Object id) {
		log.debug("Searching Conference by id {}", id);
		return persistence.findById(id);
	}

	public List<Conference> findAll() {
		return persistence.findAll();
	}

	public Conference save(final Conference t) {
		return persistence.save(t);
	}

	public void remove(final Conference t) {
		remove(t.getId());
	}

	public void remove(final Object id) {
		persistence.remove(id);
	}

	public boolean exist(final Object id) {
		return persistence.exist(id);
	}
}