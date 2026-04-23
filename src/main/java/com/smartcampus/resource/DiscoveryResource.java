package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("version", "1.0");
        response.put("description", "Smart Campus Sensor & Room Management API");
        response.put("contact", "admin@smartcampus.ac.uk");

        Map<String, Object> links = new HashMap<>();
        Map<String, String> roomsLink = new HashMap<>();
        roomsLink.put("href", "/api/v1/rooms");
        links.put("rooms", roomsLink);

        Map<String, String> sensorsLink = new HashMap<>();
        sensorsLink.put("href", "/api/v1/sensors");
        links.put("sensors", sensorsLink);

        response.put("_links", links);

        response.put("timestamp", System.currentTimeMillis());
        return Response.ok(response).build();
    }
}
