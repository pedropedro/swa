package org.swa.conf.business.access.rest.impl;

import javax.ejb.EJBException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import cz.jirutka.rsql.parser.RSQLParserException;

@Provider
public class EJBExceptionMapper implements ExceptionMapper<javax.ejb.EJBException> {

	@Override
	public Response toResponse(final EJBException e) {

		if (e.getCause().getClass() == RSQLParserException.class)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid query: " + e.getCause().getCause()
					.getMessage()).build();

		else if ("com.mongodb.MongoTimeoutException".equals(e.getCause().getClass().getName()))
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("MongoDB not running - "
					+ e.getCause().getMessage()).build();

		else
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getCause()).build();
	}
}