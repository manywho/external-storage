package com.boomi.flow.external.storage.keys;

import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;

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
    public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
        var key = keyRepository.findPlatformKey(UUID.fromString(jws.getKeyIdHeaderValue()));
        if (key == null) {
            throw new UnresolvableKeyException("Unable to resolve a platform key with the ID " + jws.getKeyIdHeaderValue());
        }

        return key.getPublicKey();
    }

    public PublicJsonWebKey resolveKeyFromPublicKey(String publicJwk) {
        try {
            return PublicJsonWebKey.Factory.newPublicJwk(publicJwk);
        } catch (JoseException e) {
            throw new RuntimeException("Unable to create a JWK instance from the public platform key", e);
        }
    }
}
