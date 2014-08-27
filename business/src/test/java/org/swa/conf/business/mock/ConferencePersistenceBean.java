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
	private final Map<Object, ConferenceCollection>	database		= new HashMap<>();

	private final AtomicLong												idGenerator	= new AtomicLong(0);

	private Class<?>																idClass;

	@Inject
	private Logger																	log;

	private Object convertId(final Object id) {
		if (id != null && id.getClass() != idClass) {
			if (idClass != null && idClass == String.class)
				return id.toString();
			else if (idClass != null && idClass == Long.class) {
				try {
					return Long.valueOf(id.toString());
				} catch (final NumberFormatException e) {
					return Long.decode("0x" + id.toString());
				}
			}
		}
		return id;
	}

	@Override
	public Conference findById(final Object id) {
		if (idClass == null && id != null) {
			idClass = id.getClass();
			log.debug("idClass: {}", idClass);
		}

		final ConferenceCollection cc = database.get(convertId(id));
		log.debug("{}ound using id {}", cc == null ? "Nothing f" : "F", id.toString());

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
		if (t.getId() == null) {
			if (idClass != null && idClass == String.class)
				t.setId(Long.toHexString(idGenerator.incrementAndGet()));
			else if (idClass != null && idClass == Long.class)
				t.setId(Long.valueOf(idGenerator.incrementAndGet()));
			else
				t.setId(new Object());
		}
		final ConferenceCollection cc = new ConferenceCollection(t);
		database.put(convertId(t.getId()), cc);
		return cc;
	}

	@Override
	public void remove(final Conference t) {
		remove(t.getId());
	}

	@Override
	public void remove(final Object id) {
		database.remove(convertId(id));
	}

	@Override
	public boolean exist(final Object id) {
		return database.containsKey(convertId(id));
	}
}