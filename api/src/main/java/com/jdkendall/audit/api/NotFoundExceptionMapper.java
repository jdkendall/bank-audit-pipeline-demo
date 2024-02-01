package com.jdkendall.audit.api;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        // Construct a 404 Not Found response
        return Response.status(Response.Status.NOT_FOUND)
                .entity("The requested resource was not found.")
                .type("text/plain")
                .build();
    }
}

