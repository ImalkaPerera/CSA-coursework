package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Maps RoomNotEmptyException → 409 Conflict
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(
                        "Room cannot be deleted",
                        "Room still has " + exception.getSensorCount() + " active sensor(s) assigned",
                        exception.getRoomId()
                ))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}