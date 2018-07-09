package com.boomi.flow.external.storage.states;

import com.boomi.flow.external.storage.utils.Environment;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.JwksDecryptionKeyResolver;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.lang.JoseException;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers.*;
import static org.jose4j.jwe.KeyManagementAlgorithmIdentifiers.*;
import static org.jose4j.jws.AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384;

public class StateManager {
    private final StateRepository repository;

    @Inject
    public StateManager(StateRepository repository) {
        this.repository = repository;
    }

    public StateResponse findState(UUID tenant, UUID id) {
        var state = repository.find(tenant, id)
                .orElseThrow(NotFoundException::new);

        PublicJsonWebKey receiverKey;
        try {
            // For now use a placeholder, but this would be a store-specific private key, used to decrypt incoming tokens
            receiverKey = PublicJsonWebKey.Factory.newPublicJwk(Environment.get("RECEIVER_KEY"));
        } catch (JoseException e) {
            throw new RuntimeException("Unable to create a JWK instance from the receiver key", e);
        }

        PublicJsonWebKey platformKey;
        try {
            // For now use a placeholder key, but this would be a public key from the platform, used to verify the integrity of any tokens
            platformKey = PublicJsonWebKey.Factory.newPublicJwk(Environment.get("PLATFORM_KEY"));
        } catch (JoseException e) {
            throw new RuntimeException("Unable to create a JWK instance from the platform key", e);
        }

        // Create the claims, which will be the content of the JWT
        JwtClaims claims = new JwtClaims();
        claims.setIssuer("receiver");  // who creates the token and signs it
        claims.setAudience("manywho"); // to whom the token is intended to be sent
        claims.setExpirationTimeMinutesInTheFuture(10); // time when the token will expire (10 minutes from now)
        claims.setGeneratedJwtId(); // a unique identifier for the token
        claims.setIssuedAtToNow();  // when the token was issued/created (now)
        claims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
        claims.setSubject(id.toString()); // the subject/principal is whom the token is about

        // Add the state metadata as claims, so any verifying consumer gets undeniably correct information
        claims.setClaim("content", state);

        // Create the signature for the actual content
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(ECDSA_USING_P384_CURVE_AND_SHA384); // TODO: Check this
        jws.setKey(receiverKey.getPrivateKey());
        jws.setKeyIdHeaderValue("24c6a8e2-fb67-412d-8adb-9cfdf286af44");
        jws.setPayload(claims.toJson());

        String signature;
        try {
            signature = jws.getCompactSerialization();
        } catch (JoseException e) {
            // TODO: Do something...
            throw new RuntimeException(e);
        }

        // The outer shell
        JsonWebEncryption jwe = new JsonWebEncryption();
        jwe.enableDefaultCompression();
        jwe.setContentTypeHeaderValue("JWT");
        jwe.setPayload(signature);
        jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.ECDH_ES_A192KW);
        jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_192_CBC_HMAC_SHA_384);
        jwe.setKey(platformKey.getKey());
        jwe.setKeyIdHeaderValue(platformKey.getKeyId());

        try {
            return new StateResponse(jwe.getCompactSerialization());
        } catch (JoseException e) {
            // TODO: Do something...
            throw new RuntimeException(e);
        }
    }

    public void saveStates(UUID tenant, List<StateRequest> stateRequests) {
        PublicJsonWebKey receiverKey;
        JsonWebKeySet jsonWebKeySetReceiverKey;
        try {
            // For now use a placeholder, but this would be a store-specific private key, used to decrypt incoming tokens
            receiverKey = PublicJsonWebKey.Factory.newPublicJwk(Environment.get("RECEIVER_KEY"));
            jsonWebKeySetReceiverKey = new JsonWebKeySet(receiverKey);
        } catch (JoseException e) {
            throw new RuntimeException("Unable to create a JWK instance from the receiver key", e);
        }

        PublicJsonWebKey platformKey;
        JsonWebKeySet jsonWebKeySetPlatform;
        try {
            // For now use a placeholder key, but this would be a public key from the platform, used to verify the integrity of any tokens
            platformKey = PublicJsonWebKey.Factory.newPublicJwk(Environment.get("PLATFORM_KEY"));
            jsonWebKeySetPlatform = new JsonWebKeySet(platformKey);
        } catch (JoseException e) {
            throw new RuntimeException("Unable to create a JWK instance from the platform key", e);
        }

        // Create constraints for the algorithms that incoming tokens need to use, otherwise decoding will fail
        var jwsAlgorithmConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, ECDSA_USING_P384_CURVE_AND_SHA384);
        var jweAlgorithmConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, ECDH_ES_A192KW, ECDH_ES_A256KW);
        var jceAlgorithmConstraints = new AlgorithmConstraints(ConstraintType.WHITELIST, AES_192_CBC_HMAC_SHA_384, AES_256_CBC_HMAC_SHA_512);

        JwksDecryptionKeyResolver jwksDecryptionKeyResolverReceiver = new JwksDecryptionKeyResolver(jsonWebKeySetReceiverKey.getJsonWebKeys());
        JwksVerificationKeyResolver jwksVerificationKeyResolverPlatform = new JwksVerificationKeyResolver(jsonWebKeySetPlatform.getJsonWebKeys());


        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setMaxFutureValidityInMinutes(300)
                .setRequireSubject()
                .setExpectedIssuer("manywho")
                .setExpectedAudience("receiver") // TODO: Change this to the store ID
                .setDecryptionKeyResolver(jwksDecryptionKeyResolverReceiver)
                .setVerificationKeyResolver(jwksVerificationKeyResolverPlatform)
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
