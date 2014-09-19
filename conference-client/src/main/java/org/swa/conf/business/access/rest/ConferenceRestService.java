package org.swa.conf.business.access.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.swa.conf.datatypes.Conference;
import org.swa.conf.datatypes.validators.Range;

@Path("")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public interface ConferenceRestService extends BaseRestService {

	final String PATH_COLLECTION = "conferences";
	final String PATH_ELEMENT = PATH_COLLECTION + "/{id}";

	@GET
	@Path(PATH_COLLECTION)
	public Response find(
			@QueryParam(QRY_PARAM_PAGE) @DefaultValue("1") @Range(min = "1", context = "parameter '" + QRY_PARAM_PAGE
					+ "' (page)") Integer page,
			@QueryParam(QRY_PARAM_ROWS) @DefaultValue("20") @Range(min = "1", context = "parameter '" + QRY_PARAM_ROWS
					+ "' (rows)") Integer rows,
			@QueryParam(QRY_PARAM_SORT) @QueryParamSortByValidator String sortBy,
			@QueryParam(QRY_PARAM_QUERY) String query
	);

	@GET
	@Path(PATH_ELEMENT)
	public Response findById(@PathParam("id") @PathParamIdValidator final String id);

	@DELETE
	@Path(PATH_ELEMENT)
	public Response delete(@PathParam("id") @PathParamIdValidator final String id);

	@PUT
	@Path(PATH_ELEMENT)
	public Response save(@PathParam("id") @PathParamIdValidator final String id, final Conference c);

	@POST
	@Path(PATH_COLLECTION)
	public Response save(final Conference c);

	@HEAD
	@Path(PATH_ELEMENT)
	public Response exist(@PathParam("id") @PathParamIdValidator final String id);
}