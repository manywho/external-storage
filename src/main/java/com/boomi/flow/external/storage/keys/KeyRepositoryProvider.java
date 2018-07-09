package com.boomi.flow.external.storage.keys;

import com.google.common.base.Strings;

import javax.inject.Provider;

public class KeyRepositoryProvider implements Provider<KeyRepository> {
    @Override
    public KeyRepository get() {
        var resolver = System.getenv("KEY_RESOLVER");
        if (Strings.isNullOrEmpty(resolver)) {
            return new KeyRepositoryEnvironment();
        }

        // TODO
        throw new RuntimeException("TODO");
    }
}
