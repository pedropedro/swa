package org.swa.conf.business.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;

import cz.jirutka.rsql.parser.ast.Node;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.AbstractDatatype;

@javax.ejb.Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class BasePersistenceBean<T extends AbstractDatatype> implements BasePersistenceService<T> {

	private final Map<Object, T> database = new HashMap<>();

	private final AtomicLong idGenerator = new AtomicLong(0);

	@Override
	public T findById(final Long id) {
		return database.get(id);
	}

	@Override
	public List<T> find(final Node queryAST, final Integer skip, final Integer limit, final String sortBy) {
		return findAll();
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
		if (t.getId() == null)
			t.setId(idGenerator.incrementAndGet());

		database.put(t.getId(), t);
		return t;
	}

	@Override
	public void remove(final T t) {
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

	@Override
	public long count(final Node queryAST) {
		return database.size();
	}
}