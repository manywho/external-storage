package com.boomi.flow.external.storage.health;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("health")
public class HealthController {
    private final HealthManager manager;

    @Inject
    public HealthController(HealthManager manager) {
        this.manager = manager;
    }

    @GET
    public Response healthCheck() {
        return manager.healthCheck();
    }
}
