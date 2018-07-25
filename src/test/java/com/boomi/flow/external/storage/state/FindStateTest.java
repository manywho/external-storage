package com.boomi.flow.external.storage.state;

import com.boomi.flow.external.storage.BaseTest;
import com.boomi.flow.external.storage.jdbi.UuidArgumentFactory;
import com.google.common.io.Resources;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
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
    @BeforeClass
    public static void init() {
        BaseTest.init();
        //Migrator.executeMigrations();
    }

    @AfterClass
    public static void stop() {
        BaseTest.stop();
        String sqlDelete = "DELETE FROM states";
        jdbi.withHandle(handle -> {
            if (databaseType().equals("mysql")) {
                handle.registerArgument(new UuidArgumentFactory());
            }

            return handle.createUpdate(sqlDelete)
                    .execute();
        });
    }

    @After
    public void cleanSate() {
        String sqlDelete = "DELETE FROM states";
        jdbi.withHandle(handle -> {
            if (databaseType().equals("mysql")) {
                handle.registerArgument(new UuidArgumentFactory());
            }

            return handle.createUpdate(sqlDelete)
                    .execute();
        });
    }

    @Test
    public void testFindState() throws URISyntaxException, IOException, JSONException, JoseException, MalformedClaimException, InvalidJwtException {
        String validStateString = new String(Files.readAllBytes(Paths.get(Resources.getResource("state/state.json").toURI())));

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");

        jdbi.useHandle(handle -> handle.createUpdate(insertState())
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

        String state = decryptToken(new JSONObject(stateEncrypted).getString("token"));
        JSONAssert.assertEquals(validStateString, state, false);
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

        Assert.assertEquals(401, response.getStatus());
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

        Assert.assertEquals(401, response.getStatus());
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

        Assert.assertEquals(400, response.getStatus());
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

        Assert.assertEquals(400, response.getStatus());
    }

    private String insertState() {
        switch (databaseType()) {
            case "mysql":
                return "INSERT INTO states (id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content) VALUES " +
                        "('4b8b27d3-e4f3-4a78-8822-12476582af8a', '918f5a24-290e-4659-9cd6-c8d95aee92c6', null, '7808267e-b09a-44b2-be2b-4216a9513b71', 'f8bfd40b-8e0b-4966-884e-ed6159aec3dc', 1, '6dc7aea2-335d-40d0-ba34-4571fb135936', '52df1a90-3826-4508-b7c2-cde8aa5b72cf', '2018-07-17 11:32:00', '2018-07-17 11:32:00', :content)";
            case "sqlserver":
                return "INSERT INTO states (id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content) VALUES " +
                        "('4b8b27d3-e4f3-4a78-8822-12476582af8a', '918f5a24-290e-4659-9cd6-c8d95aee92c6', null, '7808267e-b09a-44b2-be2b-4216a9513b71', 'f8bfd40b-8e0b-4966-884e-ed6159aec3dc', 1, '6dc7aea2-335d-40d0-ba34-4571fb135936', '52df1a90-3826-4508-b7c2-cde8aa5b72cf', '2018-07-17 11:32:00.7117030 +01:00', '2018-07-17 11:32:00.7117030 +01:00', :content)";
            default:
                return "INSERT INTO states (id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content) VALUES " +
                        "('4b8b27d3-e4f3-4a78-8822-12476582af8a', '918f5a24-290e-4659-9cd6-c8d95aee92c6', null, '7808267e-b09a-44b2-be2b-4216a9513b71', 'f8bfd40b-8e0b-4966-884e-ed6159aec3dc', true, '6dc7aea2-335d-40d0-ba34-4571fb135936', '52df1a90-3826-4508-b7c2-cde8aa5b72cf', '2018-07-17 11:32:00.7117030 +01:00', '2018-07-17 11:32:00.7117030 +01:00', :content::jsonb)";
        }
    }
}
