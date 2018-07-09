package com.boomi.flow.external.storage.keys;

import org.jose4j.jwk.PublicJsonWebKey;

import java.util.UUID;

public interface KeyRepository {
    PublicJsonWebKey findPlatformKey(UUID id);
    PublicJsonWebKey findReceiverKey(UUID id);
}
