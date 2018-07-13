package com.boomi.flow.external.storage.state;

import com.boomi.flow.external.storage.guice.JdbiProvider;
import com.boomi.flow.external.storage.Migrator;
import com.boomi.flow.external.storage.BaseTest;
import com.boomi.flow.external.storage.states.State;
import com.google.common.io.Resources;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public class StateControllerTest extends BaseTest {

    @Test
    public void testSaveStates() throws URISyntaxException, IOException, JoseException, JSONException {

        String content = new String(Files.readAllBytes(Paths.get(Resources.getResource("state/valid-state.json").toURI())));

        Migrator.executeMigrations();

        UUID tenantId = UUID.fromString("918f5a24-290e-4659-9cd6-c8d95aee92c6");
        UUID flowId = UUID.fromString("7808267e-b09a-44b2-be2b-4216a9513b71");
        UUID stateId = UUID.fromString("4b8b27d3-e4f3-4a78-8822-12476582af8a");
        UUID currentMapElementId = UUID.fromString("6dc7aea2-335d-40d0-ba34-4571fb135936");
        UUID currentUserId = UUID.fromString("52df1a90-3826-4508-b7c2-cde8aa5b72cf");
        UUID flowFersionId = UUID.fromString("f8bfd40b-8e0b-4966-884e-ed6159aec3dc");
        UUID parentId = UUID.fromString("dfcf84e6-85de-11e8-adc0-fa7ae01bbebc");

        var now = OffsetDateTime.now();

        PublicJsonWebKey plaformFull = PublicJsonWebKey.Factory.newPublicJwk(System.getenv("PLATFORM_KEY"));
        // e.g. full key: {"kty":"EC","kid":"918f5a24-290e-4659-9cd6-c8d95aee92c6","x":"4uV_ZyVYrm6QV1p7OPg-3BtOvoc_Pc6WGU8Rw4YT4MICzszxNqXCAoIT2iwOWiFO","y":"a_JUmWGAtF-xAlCLUZHmrNzsjpwLe3H4onDj6m3hKCmYRu7JIP5pNLecaw2lggcS","crv":"P-384","d":"1zK95W1S7WgHd62sr7MsN8mwmMgsVtj4jBGYgtnvMHAY7iiWUSIdtyjmCfgHGML_"}

        PublicJsonWebKey receiverFull = PublicJsonWebKey.Factory.newPublicJwk(System.getenv("RECEIVER_KEY"));
        // e.g. full key: {"kty":"EC","kid":"918f5a24-290e-4659-9cd6-c8d95aee92c6","x":"Sw9dXf0VErH_kaP7wWqsy1Iy4ahIwRDWKBG4sHtl-2Rmy4NrFpttFAK2Akcj__Gg","y":"TJAzD-yQqdvNyFTpkm6f-NC_QIz71MSeeMWOcYwKC-SBkmsI4ixNCmK6LSefq7Gs","crv":"P-384","d":"vGy3w59unwLxuWSE82ApKgJmmOSDfb-5jUQ-bRwx-7-3VXE43a0t1RJtyp8XwcZe"}

        // encrypt and sign body
        StateRequest[] requestList = createSignedEncryptedBody(
                stateId,
                tenantId,
                parentId,
                flowId,
                flowFersionId,
                true,
                currentMapElementId,
                currentUserId,
                now,
                now,
                content,
                plaformFull,
                receiverFull
        );

        MockHttpRequest request = MockHttpRequest.post("/states/918f5a24-290e-4659-9cd6-c8d95aee92c6")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(requestList));

        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);

        Assert.assertEquals(204, response.getStatus());

        var jdbi = new JdbiProvider().get();
        String sql = "SELECT id, tenant_id, parent_id, flow_id, flow_version_id, is_done, current_map_element_id, current_user_id, created_at, updated_at, content " +
                "FROM states WHERE id = :id AND tenant_id = :tenant";

        Optional<State> stateOptional = jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind("id", stateId)
                .bind("tenant", tenantId)
                .mapToBean(State.class)
                .findFirst());

        Assert.assertTrue(stateOptional.isPresent());
        Assert.assertEquals(stateId, stateOptional.get().getId());
        Assert.assertEquals(tenantId, stateOptional.get().getTenantId());
        Assert.assertEquals(parentId, stateOptional.get().getParentId());
        Assert.assertEquals(flowId, stateOptional.get().getFlowId());
        Assert.assertEquals(flowFersionId, stateOptional.get().getFlowVersionId());
        // todo fix
        // Assert.assertTrue(stateOptional.get().isDone());
        Assert.assertEquals(currentMapElementId, stateOptional.get().getCurrentMapElementId());
        Assert.assertEquals(now, stateOptional.get().getCreatedAt());
        Assert.assertEquals(now, stateOptional.get().getUpdatedAt());

        JSONAssert.assertEquals(content, stateOptional.get().getContent(),false);

        String sqlDelete = "DELETE FROM states WHERE id=:id AND tenant_id=:tenant";
        jdbi.withHandle(handle -> handle.createUpdate(sqlDelete)
                .bind("id", stateId)
                .bind("tenant", tenantId)
                .execute());
    }

    private StateRequest[] createSignedEncryptedBody(
            UUID id,
            UUID tenantId,
            UUID parentId,
            UUID flowId,
            UUID flowVersionId,
            boolean isDone,
            UUID currentMapElement,
            UUID currentUserId,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            String content,
            PublicJsonWebKey platformJwk,
            PublicJsonWebKey receiverJwk
    ) {

        // Create the claims, which will be the content of the JWT
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("manywho");  // who creates the token and signs it
        claims.setAudience(tenantId.toString()); // to whom the token is intended to be sent
        claims.setExpirationTimeMinutesInTheFuture(10); // time when the token will expire (10 minutes from now)
        claims.setGeneratedJwtId(); // a unique identifier for the token
        claims.setIssuedAtToNow();  // when the token was issued/created (now)
        claims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
        claims.setSubject(id.toString()); // the subject/principal is whom the token is about

        // Add the valid-state.json metadata as claims, so any verifying consumer gets undeniably correct information
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
