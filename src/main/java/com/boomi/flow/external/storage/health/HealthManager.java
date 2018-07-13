package com.boomi.flow.external.storage.health;

import org.jdbi.v3.core.Jdbi;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class HealthManager {
    private final Jdbi jdbi;

    @Inject
    public HealthManager(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Response healthCheck() {
        var result = jdbi.withHandle(handle -> handle.createQuery("SELECT TRUE")
                .mapTo(boolean.class)
                .findOnly());

        if (result) {
            return Response.ok().build();
        }

        return Response.serverError().build();
    }
}
