package com.boomi.flow.external.storage.common;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import java.util.UUID;

public class CommonParameters {
    @HeaderParam("X-ManyWho-Platform-Key-ID")
    private UUID publicPlatformKey;

    @HeaderParam("X-ManyWho-Receiver-Key-ID")
    private UUID publicReceiverKey;

    @PathParam("tenant")
    private UUID tenant;

    public UUID getPublicPlatformKey() {
        return publicPlatformKey;
    }

    public UUID getPublicReceiverKey() {
        return publicReceiverKey;
    }

    public UUID getTenant() {
        return tenant;
    }
}
