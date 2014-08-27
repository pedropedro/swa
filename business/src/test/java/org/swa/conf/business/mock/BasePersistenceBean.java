package org.swa.conf.business.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.AbstractDatatype;

@javax.ejb.Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class BasePersistenceBean<T extends AbstractDatatype> implements BasePersistenceService<T> {

	private final Map<Object, T>	database		= new HashMap<>();

	private final AtomicLong			idGenerator	= new AtomicLong(0);

	private Class<?>							idClass;

	@Inject
	private Logger								log;

	protected Object convertId(final Object id) {
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
	public T findById(final Object id) {
		if (idClass == null && id != null) {
			idClass = id.getClass();
			log.debug("idClass: {}", idClass);
		}

		return database.get(convertId(id));
	}

	@Override
	public List<T> findAll() {

		final List<T> l = new ArrayList<>();

		for (final T t : database.values())
			l.add(t);

		return l;
	}

	@Override
	public T save(final T t) {
		if (t.getId() == null) {
			if (idClass != null && idClass == String.class)
				t.setId(Long.toHexString(idGenerator.incrementAndGet()));
			else if (idClass != null && idClass == Long.class)
				t.setId(Long.valueOf(idGenerator.incrementAndGet()));
			else
				t.setId(new Object());
		}

		database.put(convertId(t.getId()), t);
		return t;
	}

	@Override
	public void remove(final T t) {
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