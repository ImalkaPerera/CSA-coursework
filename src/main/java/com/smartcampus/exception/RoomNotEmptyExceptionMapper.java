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
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Room cannot be deleted");
        error.put("reason", "Room still has " + exception.getSensorCount() + " active sensor(s) assigned");
        error.put("roomId", exception.getRoomId());

        return Response.status(Response.Status.CONFLICT).entity(error).build();
    }
}