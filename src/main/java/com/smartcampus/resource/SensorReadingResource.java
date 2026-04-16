package com.smartcampus.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        return Response.ok(new ArrayList<>()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createReading(Object reading) {
        return Response.status(Response.Status.CREATED).build();
    }
}
