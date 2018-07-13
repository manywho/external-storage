package com.boomi.flow.external.storage.states;

import com.boomi.flow.external.storage.common.CommonParameters;

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
     * Find a state, using its own ID and a tenant ID. If the state exists, this method will retrieve it, create a JWT
     * representation of it, sign that JWT, then encrypt the whole message, to ensure the integrity of the state data.
     *
     * @param commonParameters A bunch of various parameters from the request, including tenant ID
     * @param id               The ID of the state to find
     * @return a signed and encrypted token of the state, if one was found
     * @throws NotFoundException if no state was found
     */
    @GET
    @Path("/{tenant}/{id}")
    public StateResponse findState(@BeanParam CommonParameters commonParameters, @PathParam("id") UUID id) {
        return manager.findState(
                commonParameters.getTenant(),
                id,
                commonParameters.getPublicPlatformKey(),
                commonParameters.getPublicReceiverKey()
        );
    }

    /**
     * Save one or more states, and link them to the given tenant. As of v1 of the API, this method will only receive
     * one state at a time, but future versions may send in more than one state at a time.
     *
     * @param commonParameters A bunch of various parameters from the request, including tenant ID
     * @param states           A list of one or more states, including various metadata fields and a JWE-encrypted,
     *                         signed, JWT representation
     */
    @POST
    @Path("/{tenant}")
    public void saveStates(@BeanParam CommonParameters commonParameters, @Valid List<StateRequest> states) {
        manager.saveStates(commonParameters.getTenant(), states);
    }

    /**
     * Delete one or more states from the given tenant.
     *
     * @param commonParameters A bunch of various parameters from the request, including tenant ID
     * @param ids              An array of IDs of the states to delete
     */
    @DELETE
    @Path("/{tenant}")
    public void deleteStates(@BeanParam CommonParameters commonParameters, @Valid List<UUID> ids) {
        manager.deleteStates(commonParameters.getTenant(), ids);
    }
}
