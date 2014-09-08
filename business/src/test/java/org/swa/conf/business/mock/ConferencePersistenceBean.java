package org.swa.conf.business.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.Conference;

/** Test of returning a persistence provider specific entity subclassing corresponding domain data object */
@javax.ejb.Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Local(BasePersistenceService.class)
public class ConferencePersistenceBean implements BasePersistenceService<Conference> {

	// storage knows only persistence provider specific entities
	private final Map<Object, ConferenceCollection> database = new HashMap<>();

	private final AtomicLong idGenerator = new AtomicLong(0);

	@Inject
	private Logger log;

	@Override
	public Conference findById(final Long id) {
		final ConferenceCollection cc = database.get(id);
		log.debug("{}ound using id {}", cc == null ? "Nothing f" : "F", id);

		return cc == null ? null : new ConferenceCollection(cc);
	}

	@Override
	public List<Conference> findAll() {
		final List<Conference> l = new ArrayList<>();

		for (final ConferenceCollection t : database.values())
			l.add(t);

		return l;
	}

	@Override
	public Conference save(final Conference t) {
		if (t.getId() == null)
			t.setId(idGenerator.incrementAndGet());

		final ConferenceCollection cc = new ConferenceCollection(t);
		database.put(t.getId(), cc);
		return cc;
	}

	@Override
	public void remove(final Conference t) {
		remove(t.getId());
	}

	@Override
	public void remove(final Long id) {
		database.remove(id);
	}

	@Override
	public boolean exist(final Long id) {
		return database.containsKey(id);
	}
}