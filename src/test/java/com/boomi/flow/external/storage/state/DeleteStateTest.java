package com.boomi.flow.external.storage.state;

import com.boomi.flow.external.storage.BaseTest;
import com.boomi.flow.external.storage.state.utils.CommonStateTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.Resources;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DeleteStateTest extends BaseTest {

    @Test
    public void testDeleteState() throws URISyntaxException, IOException, JoseException {
        String validStateString = new String(Files.readAllBytes(Paths.get(Resources.getResource("state/state.json").toURI())));

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");

        CommonStateTest.cleanSates(jdbi);

        jdbi.useHandle(handle -> {
                    handle.createUpdate(CommonStateTest.insertState())
                            .bind("content", validStateString)
                            .execute();
                }
        );

        List<UUID> uuids = new ArrayList<>();
        uuids.add(stateId);

        String uri = testUrl(String.format("/states/%s", tenantId.toString()));
        Entity<String> entity = Entity.entity(objectMapper.writeValueAsString(uuids), MediaType.APPLICATION_JSON_TYPE);

        Response response = client.target(uri).request()
                .header("X-ManyWho-Platform-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Receiver-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Signature", createRequestSignature(tenantId, uri))
                .method("DELETE", entity);

        Assert.assertEquals(204, response.getStatus());

        jdbi.useHandle(handle -> {
                    int numberOfStates = handle.createQuery("SELECT COUNT(*) FROM states")
                            .mapTo(int.class)
                            .findOnly();

            Assert.assertEquals(0, numberOfStates);
                }
        );
        response.close();

        CommonStateTest.cleanSates(jdbi);
    }

    @Test
    public void testEmptySignature() throws JsonProcessingException {

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");
        List<UUID> uuids = new ArrayList<>();
        uuids.add(stateId);

        String uri = testUrl(String.format("/states/%s", tenantId.toString()));
        Entity<String> entity = Entity.entity(objectMapper.writeValueAsString(uuids), MediaType.APPLICATION_JSON_TYPE);

        Response response = client.target(uri)
                .request()
                .header("X-ManyWho-Platform-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Receiver-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .accept(MediaType.APPLICATION_JSON)
                .method("DELETE", entity);

        Assert.assertEquals(401, response.getStatus());
        response.close();
    }

    @Test
    public void testNonPlatformKey() throws JoseException, JsonProcessingException {

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");
        List<UUID> uuids = new ArrayList<>();
        uuids.add(stateId);

        String uri = testUrl(String.format("/states/%s", tenantId.toString()));
        Entity<String> entity = Entity.entity(objectMapper.writeValueAsString(uuids), MediaType.APPLICATION_JSON_TYPE);

        Response response =  client.target(uri)
                .request()
                .header("X-ManyWho-Receiver-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Signature", createRequestSignature(tenantId, uri))
                .accept(MediaType.APPLICATION_JSON)
                .method("DELETE", entity);

        Assert.assertEquals(400, response.getStatus());
        response.close();
    }

    @Test
    public void testNonReceiverKey() throws JoseException, JsonProcessingException {

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");
        List<UUID> uuids = new ArrayList<>();
        uuids.add(stateId);

        String uri = testUrl(String.format("/states/%s", tenantId.toString()));
        Entity<String> entity = Entity.entity(objectMapper.writeValueAsString(uuids), MediaType.APPLICATION_JSON_TYPE);

        Response response = client.target(uri)
                .request()
                .header("X-ManyWho-Platform-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Signature", createRequestSignature(tenantId, uri))
                .accept(MediaType.APPLICATION_JSON)
                .method("DELETE", entity);

        Assert.assertEquals(400, response.getStatus());
        response.close();
    }
}
