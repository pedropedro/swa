package org.swa.conf.business.persistence;

import java.util.List;

import cz.jirutka.rsql.parser.ast.Node;
import org.swa.conf.datatypes.AbstractDatatype;

public interface BasePersistenceService<T extends AbstractDatatype> {

	public T findById(final Long id);

	public List<T> find(Node queryAST, Integer skip, Integer limit, String sortBy);

	public long count(Node queryAST);

	public List<T> findAll();

	public T save(final T t);

	public void remove(final T t);

	public void remove(final Long id);

	public boolean exist(final Long id);
}