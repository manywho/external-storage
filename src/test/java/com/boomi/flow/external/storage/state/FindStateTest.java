package com.boomi.flow.external.storage.state;

import com.boomi.flow.external.storage.BaseTest;
import com.boomi.flow.external.storage.state.utils.CommonStateTest;
import com.google.common.io.Resources;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class FindStateTest extends BaseTest {

    @Test
    public void testFindState() throws URISyntaxException, IOException, JSONException, JoseException, MalformedClaimException, InvalidJwtException {
        String validStateString = new String(Files.readAllBytes(Paths.get(Resources.getResource("state/state.json").toURI())));

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");
        var jdbi = createJdbi();

        jdbi.useHandle(handle -> handle.createUpdate(CommonStateTest.insertState())
                            .bind("content", validStateString)
                            .execute());

        String uri = testUrl(String.format("/states/%s/%s", tenantId.toString(), stateId.toString()));

        Client client = ClientBuilder.newClient();
        String stateEncrypted = client.target(uri)
                .request()
                .header("X-ManyWho-Platform-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Receiver-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Signature", createRequestSignature(tenantId, uri))
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        client.close();

        String state = decryptToken(new JSONObject(stateEncrypted).getString("token"));
        JSONAssert.assertEquals(validStateString, state, false);

        CommonStateTest.cleanSates(jdbi);
    }

    @Test
    public void testNotValidSignature() {

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");

        String uri = testUrl(String.format("/states/%s/%s", tenantId.toString(), stateId.toString()));

        Client client = ClientBuilder.newClient();
        Response response = client.target(uri)
                .request()
                .header("X-ManyWho-Platform-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Receiver-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Signature", "not valid signature")
                .accept(MediaType.APPLICATION_JSON)
                .get();
        client.close();
        Assertions.assertEquals(401, response.getStatus());
    }

    @Test
    public void testEmptySignature() {

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");

        String uri = testUrl(String.format("/states/%s/%s", tenantId.toString(), stateId.toString()));

        Client client = ClientBuilder.newClient();
        Response response = client.target(uri)
                .request()
                .header("X-ManyWho-Platform-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Receiver-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .accept(MediaType.APPLICATION_JSON)
                .get();
        client.close();

        Assertions.assertEquals(401, response.getStatus());
    }

    @Test
    public void testNonPlatformKey() throws JoseException {

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");

        String uri = testUrl(String.format("/states/%s/%s", tenantId.toString(), stateId.toString()));

        Client client = ClientBuilder.newClient();
        Response response = client.target(uri)
                .request()
                .header("X-ManyWho-Receiver-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Signature", createRequestSignature(tenantId, uri))
                .accept(MediaType.APPLICATION_JSON)
                .get();
        client.close();

        Assertions.assertEquals(400, response.getStatus());
    }

    @Test
    public void testNonReceiverKey() throws JoseException {

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");

        String uri = testUrl(String.format("/states/%s/%s", tenantId.toString(), stateId.toString()));

        Client client = ClientBuilder.newClient();
        Response response = client.target(uri)
                .request()
                .header("X-ManyWho-Platform-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Signature", createRequestSignature(tenantId, uri))
                .accept(MediaType.APPLICATION_JSON)
                .get();
        client.close();

        Assertions.assertEquals(400, response.getStatus());
    }
}
