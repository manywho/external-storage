package com.boomi.flow.external.storage.keys;

import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.VerificationKeyResolver;

import javax.inject.Inject;
import java.security.Key;
import java.util.List;
import java.util.UUID;

public class PlatformKeyResolver implements VerificationKeyResolver {
    private final KeyRepository keyRepository;

    @Inject
    public PlatformKeyResolver(KeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    @Override
    public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) {
        return resolveKey(UUID.fromString(jws.getKeyIdHeaderValue()))
                .getPublicKey();
    }

    public PublicJsonWebKey resolveKey(UUID id) {
        var key = keyRepository.findPlatformKey(id);
        if (key == null) {
            throw new RuntimeException("Unable to resolve a platform key with the ID " + id);
        }

        return key;
    }
}
