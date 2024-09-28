package it.cnit.rest.server.controller.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NullPointerException> {
	@Override
	public Response toResponse(NullPointerException e) {

		return Response.status(Response.Status.NOT_FOUND).build();
	}
}
