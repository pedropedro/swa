package org.swa.conf.business.service;

import java.util.List;
import javax.inject.Inject;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.AbstractDatatype;

public abstract class BaseService<T extends AbstractDatatype> {

	@Inject
	private Logger log;

	@Inject
	protected BasePersistenceService<T> persistence;

	public T findById(final Long id) {
		return persistence.findById(id);
	}

	public FindResult find(final String query, final Integer page, final Integer rowsOnPage, final String sortBy) {

		if (page == null) throw new IllegalArgumentException("Page must not be null");
		if (rowsOnPage == null) throw new IllegalArgumentException("Rows must not be null");


		final Node queryAST = query == null || query.trim().isEmpty() ? null : new RSQLParser().parse(query);

		final Integer skip = page < 2 ? 0 : (page - 1) * rowsOnPage;
		final Integer limit = rowsOnPage + 1; // are there next rows ?

		return new FindResult(persistence.find(queryAST, skip, limit, sortBy), rowsOnPage);
	}

	public T save(final T t) {
		return persistence.save(t);
	}

	public void remove(final T t) {
		remove(t.getId());
	}

	public void remove(final Long id) {
		persistence.remove(id);
	}

	public boolean exist(final Long id) {
		return persistence.exist(id);
	}

	public class FindResult {

		private final List<T> rows;
		private final boolean hasNext;

		FindResult(final List<T> rows, final Integer rowsOnPage) {
			this.rows = rows;

			hasNext = rowsOnPage < rows.size();

			if (hasNext)
				this.rows.remove(this.rows.size() - 1);
		}

		public List<T> getRows() {
			return rows;
		}

		public boolean hasNext() {
			return hasNext;
		}
	}
}