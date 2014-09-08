package org.swa.conf.business.persistence;

import java.util.List;

import org.swa.conf.datatypes.AbstractDatatype;

public interface BasePersistenceService<T extends AbstractDatatype> {

	public T findById(final Long id);

	public List<T> findAll();

	public T save(final T t);

	public void remove(final T t);

	public void remove(final Long id);

	public boolean exist(final Long id);
}