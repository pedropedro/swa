package org.swa.conf.business.access.rest;

import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.swa.conf.datatypes.Conference;

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