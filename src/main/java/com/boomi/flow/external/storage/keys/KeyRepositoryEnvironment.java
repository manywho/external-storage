package com.boomi.flow.external.storage.keys;

import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.lang.JoseException;

import java.util.UUID;

public class KeyRepositoryEnvironment implements KeyRepository {
    @Override
    public PublicJsonWebKey findPlatformKey(UUID id) {
        return findKey(id, "platform");
    }

    @Override
    public PublicJsonWebKey findReceiverKey(UUID id) {
        return findKey(id, "receiver");
    }

    private static PublicJsonWebKey createKey(String json) {
        try {
            return PublicJsonWebKey.Factory.newPublicJwk(json);
        } catch (JoseException e) {
            throw new RuntimeException("Couldn't construct a JWK", e);
        }
    }

    private static PublicJsonWebKey findKey(UUID id, String type) {
        var baseName = String.format("%s_KEY", type.toUpperCase());

        // If there's a variable set with the name of just the type (e.g. PLATFORM_KEY), we use that over anything else
        if (System.getenv().containsKey(baseName)) {
            return createKey(System.getenv(baseName));
        }

        var variable = System.getenv()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(String.format("%s_KEY_%s", baseName, id)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find a " + type + " key with the " + id + " in an environment variable"));

        return createKey(variable.getValue());
    }
}
