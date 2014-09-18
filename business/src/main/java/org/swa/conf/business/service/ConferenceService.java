package org.swa.conf.business.service;

import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.Conference;

@Stateless
public class ConferenceService {

	@Inject
	private Logger log;

	@Inject
	private BasePersistenceService<Conference> persistence;

	public Conference findById(final Long id) {
		log.debug("Searching Conference by id {}", id);
		return persistence.findById(id);
	}

	public List<Conference> find(final String query, final Integer page, final Integer rowsOnPage,
			final String sortBy) {

		if (page != null && page < 1)
			throw new IllegalArgumentException("Parameter 'p' (page) must be from range <1..2147483647>");

		if (rowsOnPage != null && rowsOnPage < 1)
			throw new IllegalArgumentException("Parameter 'r' (rows pro page) must be from range <1..2147483647>");


		final Node queryAST = query == null || query.trim().isEmpty() ? null : new RSQLParser().parse(query);

		// check query actual parameters in context of logged in user
		// ...

		final Integer skip = page == null || rowsOnPage == null ? null : (page - 1) * rowsOnPage;
		final Integer limit = page == null || rowsOnPage == null ? null : rowsOnPage;

		return persistence.find(queryAST, skip, limit, sortBy);
	}

	public Conference save(final Conference t) {
		return persistence.save(t);
	}

	public void remove(final Conference t) {
		remove(t.getId());
	}

	public void remove(final Long id) {
		persistence.remove(id);
	}

	public boolean exist(final Long id) {
		return persistence.exist(id);
	}
}