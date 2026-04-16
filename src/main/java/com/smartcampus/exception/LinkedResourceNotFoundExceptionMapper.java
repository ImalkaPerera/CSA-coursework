package com.smartcampus.exception;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Dependency validation failed");
        error.put("reason", "The specified roomId does not exist in the system");
        error.put("roomId", exception.getMissingRoomId());

        return Response.status(Response.Status.UNPROCESSABLE_ENTITY).entity(error).build();
    }
}