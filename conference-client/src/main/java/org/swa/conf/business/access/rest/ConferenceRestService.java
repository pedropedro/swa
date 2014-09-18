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

@Path("")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public interface ConferenceRestService {

	final String PATH_COLLECTION = "conferences";
	final String PATH_ELEMENT = PATH_COLLECTION + "/{id}";

	@GET
	@Path(PATH_COLLECTION)
	public Response find(
			@QueryParam("p") @DefaultValue("1") final Integer page,
			@QueryParam("q") final String query,
			@QueryParam("r") @DefaultValue("20") final Integer rowsOnPage,
			@QueryParam("s") @QueryParamSortByValidator final String sortBy
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