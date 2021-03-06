package org.swa.conf.business.access.rest.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.swa.conf.business.access.rest.ConferenceRestService;
import org.swa.conf.business.access.rest.StringToLong;
import org.swa.conf.business.service.BaseService;
import org.swa.conf.business.service.ConferenceService;
import org.swa.conf.datatypes.Conference;
import org.swa.conf.datatypes.validators.ModelValidator;
import org.swa.conf.datatypes.validators.ValidationException;

@RequestScoped
public class ConferenceRestServiceBean implements ConferenceRestService {

	@Inject
	private Logger log;

	@Context
	private UriInfo ctx;

	@Context
	private Application app;

	@Context
	private SecurityContext sec;

	@Inject
	private ConferenceService s;

	@Inject
	private ModelValidator v;

	@Override
	public Response find(final Integer page, final Integer rowsOnPage, final String sortBy, final String query) {
		log.debug("find by: page={} rows={}, sort={}, query={}", page, rowsOnPage, sortBy, query);
		final BaseService.FindResult frs = s.find(query, page, rowsOnPage, sortBy);
		return Response.ok().entity(frs.getRows()).links(
				getPrevNextLink("prev", "" + (page < 2 ? 1 : page - 1), rowsOnPage.toString(), sortBy, query),
				getParentLink(),
				getPrevNextLink("next", "" + (frs.hasNext() ? page + 1 : page), rowsOnPage.toString(), sortBy, query)
		).header("hasNext", frs.hasNext()).build();
	}

	@Override
	public Response findById(final String _id) {

		log.debug("findById: {}", _id);
		final Long id = StringToLong.decode(_id);

		if (id != null) {
			final Conference c = s.findById(id);

			if (c != null)
				return Response.ok().entity(c).links(getParentLink(), getSelfLink(id)).build();
		}
		return Response.status(Response.Status.NOT_FOUND).links(getParentLink()).build();
	}

	@Override
	public Response delete(final String _id) {

		log.debug("delete id:{}", _id);
		final Long id = StringToLong.decode(_id);

		if (id != null)
			s.remove(id);

		return Response.ok().links(getParentLink()).build();
	}

	@Override
	public Response save(final String _id, final Conference c) {

		final Long id = StringToLong.decode(_id);

		if (id != null && id.equals(c.getId()))
			return save(c);
		else
			return Response.status(Response.Status.BAD_REQUEST).links(getParentLink())
					.entity("ID in path :" + id + " and from entity :" + c.getId() + " doesn't match").build();
	}

	@Override
	public Response save(final Conference c) {

		try {
			v.validate(c);
		} catch (final ValidationException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getLocalizedMessageJson()).build();
		}

		final Conference saved = s.save(c);

		final Link self = getSelfLink(saved.getId());
		return Response.status(Response.Status.CREATED).location(self.getUri()).links(self).build();
	}

	@Override
	public Response exist(final String _id) {

		final Long id = StringToLong.decode(_id);

		return Response.status(s.exist(id) ? Response.Status.OK : Response.Status.GONE).build();
	}

	// ------------------------------------------------------------
	// HATEOAS - propose the client little navigation possibilities
	// ------------------------------------------------------------
	private String getBaseUri() {
		return ctx.getBaseUri().getPath() + ConferenceRestService.PATH_COLLECTION;
	}

	private Link getSelfLink(final Long id) {
		return Link.fromPath(getBaseUri() + "/" + id).rel("self").type(MediaType.APPLICATION_JSON).build();
	}

	private Link getParentLink() {
		// log.debug(ctx.getBaseUri().getPath());// /business-ejb/
		// log.debug(Link.fromMethod(ConferenceRestService.class, "getAll").build().getUri().getPath());// conferences
		return Link.fromPath(getBaseUri()).rel("parent").type(MediaType.APPLICATION_JSON).build();
	}

	private Link getPrevNextLink(final String relation, final String page, final String rows, final String sortBy,
			final String query) {

		final Link.Builder b = Link.fromPath(getBaseUri()).rel(relation).type(MediaType.APPLICATION_JSON);

		b.param(QRY_PARAM_PAGE, page).param(QRY_PARAM_ROWS, rows);

		if (sortBy != null) b.param(QRY_PARAM_SORT, sortBy);

		if (query != null) b.param(QRY_PARAM_QUERY, query);

		return b.build();
	}
}