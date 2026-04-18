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
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Sensor unavailable");
        error.put("reason", "Sensor is currently in MAINTENANCE and cannot accept readings");
        error.put("sensorId", exception.getSensorId());

        return Response.status(Response.Status.FORBIDDEN).entity(error).build();
    }
}