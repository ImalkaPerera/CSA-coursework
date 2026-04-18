package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;

// Sub-resource for sensor readings (no @Path — resolved via locator)
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings — reading history
    @GET
    public Response getReadings() {
        List<SensorReading> readings = DataStore.getInstance().getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings — add reading + update currentValue
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createReading(SensorReading reading, @Context UriInfo uriInfo) {
        Sensor sensor = DataStore.getInstance().getSensor(sensorId);
        if (sensor != null && sensor.getStatus() != null && sensor.getStatus().equalsIgnoreCase("MAINTENANCE")) {
            throw new SensorUnavailableException(sensorId);
        }

        if (reading == null) {
            reading = new SensorReading();
        }

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() == 0L) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        DataStore.getInstance().addReading(sensorId, reading);

        // Update parent sensor's currentValue (thread-safe via synchronized block)
        if (sensor != null) {
            synchronized (sensor) {
                sensor.setCurrentValue(reading.getValue());
            }
        }

        URI location = uriInfo.getAbsolutePathBuilder().path(reading.getId()).build();
        return Response.created(location)
                .entity(reading)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
