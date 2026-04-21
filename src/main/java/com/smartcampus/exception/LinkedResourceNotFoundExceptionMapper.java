package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Maps LinkedResourceNotFoundException → 422 Unprocessable Entity
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        return Response.status(422)
                .entity(new ErrorResponse(
                        "Dependency validation failed",
                        "The specified roomId does not exist in the system",
                        exception.getMissingRoomId()
                ))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}