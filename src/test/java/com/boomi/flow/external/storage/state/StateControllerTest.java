package com.boomi.flow.external.storage.state;

import com.boomi.flow.external.storage.BaseTest;
import com.boomi.flow.external.storage.Migrator;
import com.boomi.flow.external.storage.guice.HikariDataSourceProvider;
import com.boomi.flow.external.storage.guice.JdbiProvider;
import com.boomi.flow.external.storage.states.State;
import com.boomi.flow.external.storage.utils.UuidArgumentFactory;
import com.google.common.io.Resources;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.skyscreamer.jsonassert.JSONAssert;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StateControllerTest extends BaseTest {
    @BeforeClass
    public static void init() {
        BaseTest.init();

        Migrator.executeMigrations();
    }

    @AfterClass
    public static void stop() {
        BaseTest.stop();

        var jdbi = new JdbiProvider(new HikariDataSourceProvider().get()).get();
        String sqlDelete = "DELETE FROM states";
        jdbi.withHandle(handle -> {
            if (isMysqlDatabase()) {
                handle.registerArgument(new UuidArgumentFactory());
            }

            return handle.createUpdate(sqlDelete)
                    .execute();
        });
    }

    @After
    public void cleanSate() {
        var jdbi = new JdbiProvider(new HikariDataSourceProvider().get()).get();
        String sqlDelete = "DELETE FROM states";
        jdbi.withHandle(handle -> {
            if (isMysqlDatabase()) {
                handle.registerArgument(new UuidArgumentFactory());
            }

            return handle.createUpdate(sqlDelete)
                    .execute();
        });
    }

    @Test
    public void testDeleteState() throws URISyntaxException, IOException, JoseException {
        String validStateString = new String(Files.readAllBytes(Paths.get(Resources.getResource("state/state.json").toURI())));

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");
        var jdbi = new JdbiProvider(new HikariDataSourceProvider().get()).get();

        jdbi.useHandle(handle -> {
                    handle.createUpdate(
                            "INSERT INTO states (id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content) VALUES " +
                                    "('4b8b27d3-e4f3-4a78-8822-12476582af8a', '918f5a24-290e-4659-9cd6-c8d95aee92c6', null, '7808267e-b09a-44b2-be2b-4216a9513b71', 'f8bfd40b-8e0b-4966-884e-ed6159aec3dc', 1, '6dc7aea2-335d-40d0-ba34-4571fb135936', '52df1a90-3826-4508-b7c2-cde8aa5b72cf', '2018-07-17 11:32:00.7117030 +01:00', '2018-07-17 11:32:00.7117030 +01:00', :content)")
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
    }

    @Test
    public void testFindState() throws URISyntaxException, IOException, JSONException, JoseException, MalformedClaimException, InvalidJwtException {
        String validStateString = new String(Files.readAllBytes(Paths.get(Resources.getResource("state/state.json").toURI())));

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");
        var jdbi = new JdbiProvider(new HikariDataSourceProvider().get()).get();

        jdbi.useHandle(handle -> {
                    handle.createUpdate(
                            "INSERT INTO states (id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content) VALUES " +
                                    "('4b8b27d3-e4f3-4a78-8822-12476582af8a', '918f5a24-290e-4659-9cd6-c8d95aee92c6', null, '7808267e-b09a-44b2-be2b-4216a9513b71', 'f8bfd40b-8e0b-4966-884e-ed6159aec3dc', 1, '6dc7aea2-335d-40d0-ba34-4571fb135936', '52df1a90-3826-4508-b7c2-cde8aa5b72cf', '2018-07-17 11:32:00.7117030 +01:00', '2018-07-17 11:32:00.7117030 +01:00', :content)")
                            .bind("content", validStateString)
                            .execute();
                }
        );

        String uri = testUrl(String.format("/states/%s/%s", tenantId.toString(), stateId.toString()));

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
    public void testSaveStates() throws URISyntaxException, IOException, JoseException, JSONException {

        String content = new String(Files.readAllBytes(Paths.get(Resources.getResource("state/state.json").toURI())));

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");
        UUID flowId = UUID.fromString("7808267e-b09a-44b2-be2b-4216a9513b71");
        UUID currentMapElementId = UUID.fromString("6dc7aea2-335d-40d0-ba34-4571fb135936");
        UUID currentUserId = UUID.fromString("52df1a90-3826-4508-b7c2-cde8aa5b72cf");
        UUID flowVersionId = UUID.fromString("f8bfd40b-8e0b-4966-884e-ed6159aec3dc");
        UUID parentId = UUID.fromString("dfcf84e6-85de-11e8-adc0-fa7ae01bbebc");

        OffsetDateTime now = OffsetDateTime.now();

        if (isMysqlDatabase()) {
            // my sql doesn't save the nanoseconds, also seems to truncate with a round up or round down,
            // I have set nano to 0 to avoid problems for now
            now = now.withNano(0);
        }

        // you can find examples of the following keys at test/resources/example-key
        PublicJsonWebKey plaformFull = PublicJsonWebKey.Factory.newPublicJwk(System.getenv("PLATFORM_KEY"));
        PublicJsonWebKey receiverFull = PublicJsonWebKey.Factory.newPublicJwk(System.getenv("RECEIVER_KEY"));

        // encrypt and sign body
        StateRequest[] requestList = createSignedEncryptedBody(stateId, tenantId, parentId, flowId, flowVersionId,
                false, currentMapElementId, currentUserId, now, now, content, plaformFull, receiverFull);

        String url = testUrl("/states/918f5a24-290e-4659-9cd6-c8d95aee92c6");
        Entity<String> entity = Entity.entity(objectMapper.writeValueAsString(requestList), MediaType.APPLICATION_JSON_TYPE);

        Response response = client.target(url).request()
                .header("X-ManyWho-Platform-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Receiver-Key-ID", "918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .header("X-ManyWho-Signature", createRequestSignature(tenantId, url))
                .post(entity);


        Assert.assertEquals(204, response.getStatus());

        var jdbi = new JdbiProvider(new HikariDataSourceProvider().get()).get();
        String sql = "SELECT id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content " +
                "FROM states WHERE id = :id AND tenant_id = :tenant";

        Optional<State> stateOptional = jdbi.withHandle(handle -> {
            if (isMysqlDatabase()) {
                handle.registerArgument(new UuidArgumentFactory());
            }

            return handle.createQuery(sql)
                    .bind("id", stateId)
                    .bind("tenant", tenantId)
                    .mapToBean(State.class)
                    .findFirst();
        });

        Assert.assertTrue(stateOptional.isPresent());
        Assert.assertEquals(stateId, stateOptional.get().getId());
        Assert.assertEquals(tenantId, stateOptional.get().getTenantId());
        Assert.assertEquals(parentId, stateOptional.get().getParentId());
        Assert.assertEquals(flowId, stateOptional.get().getFlowId());
        Assert.assertEquals(flowVersionId, stateOptional.get().getFlowVersionId());
        // todo fix
        // Assert.assertTrue(stateOptional.get().isDone());
        Assert.assertEquals(currentMapElementId, stateOptional.get().getCurrentMapElementId());

        if (isMysqlDatabase()) {
            Timestamp nowWithoutTimezone = Timestamp.valueOf(now.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
            Timestamp created_at = Timestamp.valueOf(stateOptional.get().getCreatedAt().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
            Timestamp updated_at = Timestamp.valueOf(stateOptional.get().getUpdatedAt().atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
            Assert.assertEquals(nowWithoutTimezone, created_at);
            Assert.assertEquals(nowWithoutTimezone, updated_at);
        } else {
            Assert.assertEquals(now, stateOptional.get().getCreatedAt());
            Assert.assertEquals(now, stateOptional.get().getUpdatedAt());
        }

        JSONAssert.assertEquals(content, stateOptional.get().getContent(), false);
    }

    private StateRequest[] createSignedEncryptedBody(UUID id, UUID tenantId, UUID parentId, UUID flowId, UUID flowVersionId,
                                                     boolean isDone, UUID currentMapElement, UUID currentUserId,
                                                     OffsetDateTime createdAt, OffsetDateTime updatedAt, String content,
                                                     PublicJsonWebKey platformJwk, PublicJsonWebKey receiverJwk) {

        // Create the claims, which will be the content of the JWT
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("manywho");  // who creates the token and signs it
        claims.setAudience(tenantId.toString()); // to whom the token is intended to be sent
        claims.setExpirationTimeMinutesInTheFuture(10); // time when the token will expire (10 minutes from now)
        claims.setGeneratedJwtId(); // a unique identifier for the token
        claims.setIssuedAtToNow();  // when the token was issued/created (now)
        claims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
        claims.setSubject(id.toString()); // the subject/principal is whom the token is about

        // Add the state.json metadata as claims, so any verifying consumer gets undeniably correct information
        claims.setClaim("id", id);
        claims.setClaim("tenant", tenantId);
        claims.setClaim("parent", parentId);
        claims.setClaim("flow", flowId);
        claims.setClaim("flowVersion", flowVersionId);
        claims.setClaim("isDone", isDone);
        claims.setClaim("currentMapElement", currentMapElement);
        claims.setClaim("currentUser", currentUserId);
        claims.setClaim("createdAt", createdAt);
        claims.setClaim("updatedAt", updatedAt);
        claims.setClaim("content", content);

        // Create the signature for the actual content
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384); // TODO: Check this
        jws.setKey(platformJwk.getPrivateKey());
        jws.setKeyIdHeaderValue(platformJwk.getKeyId());
        jws.setPayload(claims.toJson());

        String signedPayload;
        try {
            signedPayload = jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException("Unable to serialize the JWS", e);
        }

        // The outer shell
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.enableDefaultCompression();
        jwe.setContentTypeHeaderValue("JWT");
        jwe.setPayload(signedPayload);
        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.ECDH_ES_A192KW);
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_192_CBC_HMAC_SHA_384);
        jwe.setKey(receiverJwk.getKey());
        jwe.setKeyIdHeaderValue(receiverJwk.getKeyId());

        String serializedJwe;
        try {
            serializedJwe = jwe.getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException("Unable to serialize the JWE", e);
        }

        // Create the payload that the external store is expecting
        return new StateRequest[]{
                new StateRequest(serializedJwe)
        };
    }
}
