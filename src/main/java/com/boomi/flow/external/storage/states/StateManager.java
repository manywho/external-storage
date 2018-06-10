package com.boomi.flow.external.storage.states;

import com.boomi.flow.external.storage.utils.Environment;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers.*;
import static org.jose4j.jwe.KeyManagementAlgorithmIdentifiers.*;
import static org.jose4j.jws.AlgorithmIdentifiers.RSA_USING_SHA384;

public class StateManager {
    private final StateRepository repository;

    @Inject
    public StateManager(StateRepository repository) {
        this.repository = repository;
    }

    public StateResponse findState(UUID tenant, UUID id) {
        return repository.find(tenant, id)
                .orElseThrow(NotFoundException::new);
    }

    public void saveStates(UUID tenant, List<StateRequest> stateRequests) {
        PublicJsonWebKey receiverKey;
        try {
            // For now use a placeholder, but this would be a store-specific private key, used to decrypt incoming tokens
            receiverKey = PublicJsonWebKey.Factory.newPublicJwk(Environment.get("RECEIVER_KEY"));
        } catch (JoseException e) {
            throw new RuntimeException("Unable to create a JWK instance from the receiver key", e);
        }

        PublicJsonWebKey platformKey;
        try {
            // For now use a placeholder key, but this would be a public key from the Engine, used to verify the integrity of any tokens
            platformKey = PublicJsonWebKey.Factory.newPublicJwk(Environment.get("PLATFORM_KEY"));
        } catch (JoseException e) {
            throw new RuntimeException("Unable to create a JWK instance from the sender key", e);
        }

        // Create constraints for the algorithms that incoming tokens need to use, otherwise decoding will fail
        var jwsAlgorithmConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, RSA_USING_SHA384);
        var jweAlgorithmConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, RSA_OAEP, RSA_OAEP_256, ECDH_ES, ECDH_ES_A192KW, ECDH_ES_A256KW);
        var jceAlgorithmConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, AES_192_GCM, AES_192_CBC_HMAC_SHA_384, AES_256_GCM, AES_256_CBC_HMAC_SHA_512);

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setMaxFutureValidityInMinutes(300)
                .setRequireSubject()
                .setExpectedIssuer("manywho")
                .setExpectedAudience("receiver") // TODO: Change this to the store ID
                .setDecryptionKey(receiverKey.getPrivateKey())
                .setVerificationKey(platformKey.getKey())
                .setJwsAlgorithmConstraints(jwsAlgorithmConstraints)
                .setJweAlgorithmConstraints(jweAlgorithmConstraints)
                .setJweContentEncryptionAlgorithmConstraints(jceAlgorithmConstraints)
                .build();

        List<State> states = new ArrayList<>();

        for (StateRequest stateRequest : stateRequests) {
            // Verify, decrypt, and then decode the claims from the incoming state request's token
            JwtClaims claims;
            try {
                claims = jwtConsumer.processToClaims(stateRequest.getToken());
            }
            catch (InvalidJwtException e) {
                throw new RuntimeException("An invalid JWT was given", e);
            }

            State state = new State();

            try {
                // Set the properties of the metadata we want to store, using the decoded and verified token's claims
                state.setContent(claims.getStringClaimValue("content"));
                state.setCreatedAt(OffsetDateTime.parse(claims.getStringClaimValue("createdAt")));
                state.setCurrentMapElementId(uuidFromNullableString(claims.getStringClaimValue("currentMapElement")));
                state.setCurrentUserId(uuidFromNullableString(claims.getStringClaimValue("currentUser")));
                state.setDone(claims.getClaimValue("isDone", Boolean.class));
                state.setFlowId(uuidFromNullableString(claims.getStringClaimValue("flow")));
                state.setFlowVersionId(uuidFromNullableString(claims.getStringClaimValue("flowVersion")));
                state.setId(uuidFromNullableString(claims.getStringClaimValue("id")));
                state.setParentId(uuidFromNullableString(claims.getStringClaimValue("parent")));
                state.setTenantId(uuidFromNullableString(claims.getStringClaimValue("tenant")));
                state.setToken(stateRequest.getToken());
                state.setUpdatedAt(OffsetDateTime.parse(claims.getStringClaimValue("updatedAt")));
            } catch (MalformedClaimException e) {
                throw new RuntimeException("Unable to load the state content from the incoming claims", e);
            }

            states.add(state);
        }

        repository.save(tenant, states);
    }

    // TODO: Move this to the SDK
    private static UUID uuidFromNullableString(String value) {
        if (value == null) {
            return null;
        }

        return UUID.fromString(value);
    }
}
