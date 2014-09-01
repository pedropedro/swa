package org.swa.conf.business.access.rest;

import org.swa.conf.datatypes.Conference;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("")
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
public interface ConferenceRestService {

	final String	PATH_COLLECTION	= "conferences";
	final String	PATH_ELEMENT		= ConferenceRestService.PATH_COLLECTION + "/{id}";

	@GET
	@Path(ConferenceRestService.PATH_COLLECTION)
	public List<Conference> getAll();

	@GET
	@Path(ConferenceRestService.PATH_ELEMENT)
	public Response getOne(@PathParam("id") @PathParamIdValidator final String id);

	@DELETE
	@Path(ConferenceRestService.PATH_ELEMENT)
	public Response delete(@PathParam("id") @PathParamIdValidator final String id);

	@PUT
	@Path(ConferenceRestService.PATH_ELEMENT)
	public Response save(@PathParam("id") @PathParamIdValidator final String id, @Valid final Conference c);

	@POST
	@Path(ConferenceRestService.PATH_COLLECTION)
	public Response save(final Conference c);

	@HEAD
	@Path(ConferenceRestService.PATH_ELEMENT)
	public Response exist(@PathParam("id") @PathParamIdValidator final String id);
}