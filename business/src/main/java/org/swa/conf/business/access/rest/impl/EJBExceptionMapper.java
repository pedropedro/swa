package org.swa.conf.business.access.rest.impl;

import javax.ejb.EJBException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EJBExceptionMapper implements ExceptionMapper<javax.ejb.EJBException> {

	@Override
	public Response toResponse(final EJBException e) {

		ResponseBuilder rb = Response.status(Response.Status.INTERNAL_SERVER_ERROR);

		if (e.getClass() == javax.ejb.EJBTransactionRolledbackException.class) {

			if (e.getCause().getClass() == NumberFormatException.class)
				// rb = rb.entity("Timeout. Is database running ?");
				rb = rb.entity(e.getCause());
			else
				rb = rb.entity(e.getCause());
		}

		return rb.build();
	}
}