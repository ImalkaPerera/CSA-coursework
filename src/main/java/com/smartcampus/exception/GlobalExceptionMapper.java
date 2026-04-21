package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

// Catches all unhandled exceptions → 500 (no stack trace in response)
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.warning(exception.getClass().getName() + ": " + exception.getMessage());

        if (exception instanceof javax.ws.rs.WebApplicationException) {
            Response response = ((javax.ws.rs.WebApplicationException) exception).getResponse();
            if (!response.hasEntity()) {
                return Response.fromResponse(response)
                        .entity(new ErrorResponse(
                                "HTTP " + response.getStatus(),
                                exception.getMessage()
                        ))
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
            return response;
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(
                        "Internal Server Error",
                        "An unexpected error occurred. Please contact support.",
                        ""
                ))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}