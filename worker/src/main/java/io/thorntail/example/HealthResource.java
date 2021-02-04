package io.thorntail.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/health")
public class HealthResource {
    @GET
    public String getName() {
        return "ok";
    }
}
