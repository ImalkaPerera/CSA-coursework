package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("version", "1.0");
        response.put("description", "Smart Campus Sensor & Room Management API");
        response.put("contact", "admin@smartcampus.ac.uk");

        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("links", links);

        response.put("timestamp", System.currentTimeMillis());
        return Response.ok(response).build();
    }
}
