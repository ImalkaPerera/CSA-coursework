package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /api/v1/sensors — list all, optionally filtered by ?type= (case-insensitive)
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        ArrayList<Sensor> sensors = new ArrayList<>(DataStore.getInstance().getAllSensors());

        if (type == null || type.trim().isEmpty()) {
            return Response.ok(sensors).build();
        }

        ArrayList<Sensor> filteredSensors = new ArrayList<>();
        for (Sensor sensor : sensors) {
            if (sensor.getType() != null && sensor.getType().equalsIgnoreCase(type)) {
                filteredSensors.add(sensor);
            }
        }

        return Response.ok(filteredSensors).build();
    }

    // POST /api/v1/sensors — create sensor (validates roomId exists)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid sensor data", "Request body is required"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (sensor.getName() == null || sensor.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid sensor data", "Sensor name is required"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (sensor.getRoomId() == null || DataStore.getInstance().getRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }

        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            sensor.setId(UUID.randomUUID().toString());
        }

        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        if (DataStore.getInstance().getSensor(sensor.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Sensor already exists", "A sensor with this ID already exists", sensor.getId()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        DataStore.getInstance().upsertSensor(sensor);

        Room room = DataStore.getInstance().getRoom(sensor.getRoomId());
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location)
                .entity(sensor)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // GET /api/v1/sensors/{sensorId} — get sensor by ID
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getInstance().getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Sensor not found", "No sensor exists with the given ID", sensorId))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok(sensor).build();
    }

    // Sub-resource locator — delegates to SensorReadingResource
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadings(@PathParam("sensorId") String sensorId) {
        if (DataStore.getInstance().getSensor(sensorId) == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(new ErrorResponse("Sensor not found", "No sensor exists with the given ID", sensorId))
                            .type(MediaType.APPLICATION_JSON)
                            .build()
            );
        }

        return new SensorReadingResource(sensorId);
    }
}
