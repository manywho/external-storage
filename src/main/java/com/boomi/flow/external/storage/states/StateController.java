package com.boomi.flow.external.storage.states;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

@Path("/states")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StateController {
    private final StateManager manager;

    @Inject
    public StateController(StateManager manager) {
        this.manager = manager;
    }

    /**
     * Find a state, using its own ID and a tenant ID. If the state exists, this method will return the original signed
     * JWT representation that was sent by the Boomi Flow platform, to ensure the integrity of the state data.
     *
     * @param tenant The ID of the tenant the state is from
     * @param id     The ID of the state to find
     * @return the original JWT representation of the state, if one was found
     * @throws NotFoundException if no state was found
     */
    @GET
    @Path("/{tenant}/{id}")
    public StateResponse findState(
            @PathParam("tenant") UUID tenant,
            @PathParam("id") UUID id,
            @HeaderParam("X-ManyWho-Platform-Key") String publicPlatformKey,
            @HeaderParam("X-ManyWho-Receiver-Key") String publicReceiverKey
    ) {
        return manager.findState(tenant, id, publicPlatformKey, publicReceiverKey);
    }

    /**
     * Save one or more states, and link them to the given tenant. As of v1 of the API, this method will only receive
     * one state at a time, but future versions may send in more than one state at a time.
     *
     * @param tenant The ID of the tenant the states are from
     * @param states A list of one or more states, including various metadata fields and a JWE-encrypted, signed, JWT
     *               representation
     */
    @POST
    @Path("/{tenant}")
    public void saveStates(@PathParam("tenant") UUID tenant, @Valid List<StateRequest> states) {
        manager.saveStates(tenant, states);
    }

    @DELETE
    @Path("/{tenant}")
    public void deleteStates(@PathParam("tenant") UUID tenant, @Valid List<UUID> ids) {
        manager.deleteStates(tenant, ids);
    }
}
