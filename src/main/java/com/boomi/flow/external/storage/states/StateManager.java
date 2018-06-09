package com.boomi.flow.external.storage.states;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.JsonWebKey;
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
            receiverKey = PublicJsonWebKey.Factory.newPublicJwk("{\"kty\":\"RSA\",\"kid\":\"6aa09aee-92ea-4332-81fb-e7a70546b502\",\"n\":\"pP9k3atbDFMlLeso91YPFWz2CVWa-xokmm9eqIyYXb3lEvw0YRCWNL75l2H264-EBD5OWCpvrCuNlQoKTq_epH_IQv7FC_sAMITrJw-yC4Zuh-5_HgiTUHstXjPLxBXS2ATeE6ogzIMMiVNf8-BWABA22KZuL2Fhx_VunlKU-Mz1CKBjAaBdpWmb7SEifuRnUa23Q19KsAzXd-ZHcfeNVYG7UmN_I4V_0dOESMde21966QnLwG5lkvHD3C0Ipo7p3MME9FomWq7eFM_tXR7i7Mwuj4vxeG6hQRaPRnKTQUWHis-Thg1spF6kcVV1RK2FaDeBGgFIuLEnn45o1Dqpnw\",\"e\":\"AQAB\",\"d\":\"L5u5oPR2mwHKsosuEN_2DjqYa20WAOo0MZN8_qlCtZm7ZVT8UB2XGbUZE4Mi2ilY8FiCBpKIEmemvKBfBQFd_p0YTaxxctmD3nwb1fODbivl7Lb2WtYim5BPMOVFIFkjs00EPyxRtnEBxwYVo6rRZcdH7A9pr6YrrqS0vF4bugYCusJIWH9wbBQIVec01fTF9ncOE8FQ71CYM9v5WxnrET0fT52lo456lG4nWNPEsSZz3Pv-qhfzhVV37lKvg1ZPyx5LJR3zIEyzXBfnDRk6NwEblmroZYXBIcmTlJxH9EXxJPmg1dA2_NokDJK1xrOC2_WhQI7Ez0k3L1kIMMGIIQ\",\"p\":\"21wnEpseZKCa3JeCAVDAMYi1BVd_FNreVGoOd2E4BQRQ2txa6pcQfKoDbG80U8FjMBpVHqxmitB6Z7WYDWZiqRMPSS4zoAoPiwkXr5amf9Xfkx4ku_pq0nuBAV9WpJQbL0Je6nASv17NDwsqt2qHVnmtZc49k2Lv88ZanjRvbCk\",\"q\":\"wI60PiQRKcnbmku-xWDOI0aPvhguBCrh5Pr--YfdGMbpxM6OzeDng2aClCQLWMvNfeghtuDgYiyrpycrVafm5ai5CWhmXZ3s9G7MpA2s5V_SLGKc-FmDz_gm4LKOpONKfdMOin96i03yZNDVqk-_i6xHYDpaxZRhWTSGg4bHoIc\",\"dp\":\"0kF9Dh9yvV8XsLLkIKCm55OydwFLxNCY5G6XmSOtT1m4ql8mIc9UNTm8eFYK4PDvQq8qwXDcBNgZS4jKyqVFkeu77hgD0bVy-oBnnJ0Y0FVOrDPrX-aCN2e72nXolW2EtQK3-ZwczCNxB6dbdVz9hgyxAHDzom7lslKAjz2RQyk\",\"dq\":\"syCNn_khVkSH24N3Flo1qS8s7OPFeu8BcWfk5fBzx2bTdNfKlM8Rz3T_KBxXyeTdZrEZG-0aD3oKvNZ9Q5OMAjzw1UWfZJtTIRgpmrt2CXMYK14ZNBbhvgfU8tZoSA-A7of2UPTB5PE_-nUjLuo9AAvl4iim5IJiBQAN2jD5Z-U\",\"qi\":\"F1qBgRNLDUkdbE9g6fLGUJsPzK2HZQx1OqUim9ntwNgbb_Dea2lIOc-VPchBkV2WUabH64WD8QwOnUNJ6OU5YPIHT8uKPuRGD89bPwJ3CvWngIsCwVGcOmgQmp-PWx85GEZgqK-4zhsZezBeuJfKJUreA7Npyr6ErMslVgSVYQg\"}");
        } catch (JoseException e) {
            throw new RuntimeException("Unable to create a JWK instance from the receiver key", e);
        }

        JsonWebKey senderKey;
        try {
            // For now use a placeholder key, but this would be a public key from the Engine, used to verify the integrity of any tokens
            senderKey = JsonWebKey.Factory.newJwk("{\"kty\":\"RSA\",\"kid\":\"24c6a8e2-fb67-412d-8adb-9cfdf286af44\",\"n\":\"n0985jzxf6koZvEqoTTnPbr8TcwfV_iyAIrzfNHO4F38ASgXlbvORHbcV3dA1llv0XU3j7QY53sQi4bZjJBXDpfSmOKcFg4IhZkongMxiOIFJOyVp5DSC2-i7tVe6annEl71iMmsLn4yq74bQfxo2oTasKwmzHp4aLJvVKrYSPBSC9L1aye3oZyXqqRix5RkYcCFupOUzqtNojl2RnB_N5Vzz4bfhITX9YvmPMSsLxPAv24mWgkgi_ZH9Y6dbWNZmXxA2rM4KUSQmGcx1zdGG8g_FsSV3iaks9tICdKkiu9OEiT2G6phXL73_7Dr9qleEgNiok08eF-_X9HIpsh0Vw\",\"e\":\"AQAB\"}");
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
                .setVerificationKey(senderKey.getKey())
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
