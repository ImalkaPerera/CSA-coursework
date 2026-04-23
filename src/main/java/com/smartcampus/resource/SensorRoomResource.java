package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Resource for managing rooms. 
 * Named SensorRoomResource to align with the coursework requirements document (line 112).
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    // GET /api/v1/rooms — list all rooms
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(DataStore.getInstance().getAllRooms())).build();
    }

    // POST /api/v1/rooms — create a room (returns 201 + Location header)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null || room.getName() == null || room.getName().trim().isEmpty() || room.getCapacity() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid room data", "name and a positive capacity are required"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (room.getId() == null || room.getId().trim().isEmpty()) {
            room.setId(UUID.randomUUID().toString());
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        if (DataStore.getInstance().getRoom(room.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Room already exists", "A room with this ID already exists", room.getId()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        DataStore.getInstance().upsertRoom(room);

        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location)
                .entity(room)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // GET /api/v1/rooms/{roomId} — get room by ID
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.getInstance().getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Room not found", "No room exists with the given ID", roomId))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} — delete room (409 if has sensors, 204 if empty)
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.getInstance().getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Room not found", "No room exists with the given ID", roomId))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        long sensorCount = DataStore.getInstance().getAllSensors().stream()
                .filter(sensor -> roomId.equals(sensor.getRoomId()))
                .count();

        if (sensorCount > 0) {
            throw new RoomNotEmptyException(roomId, (int) sensorCount);
        }

        DataStore.getInstance().deleteRoom(roomId);
        return Response.noContent().build();
    }
}
