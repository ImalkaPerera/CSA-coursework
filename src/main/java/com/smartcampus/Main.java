package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static HttpServer startServer() {
        // Create a resource config that scans for JAX-RS resources and providers
        // in com.smartcampus package
        final ResourceConfig rc = ResourceConfig.forApplicationClass(SmartCampusApplication.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Smart Campus API started at %s", BASE_URI));
        System.out.println("Hit enter to stop it...");
        System.in.read();
        server.shutdownNow();
    }
}
