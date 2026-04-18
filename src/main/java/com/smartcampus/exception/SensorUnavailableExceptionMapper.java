package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Maps SensorUnavailableException → 403 Forbidden
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse(
                        "Sensor unavailable",
                        "Sensor is currently in MAINTENANCE and cannot accept readings",
                        exception.getSensorId()
                ))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}