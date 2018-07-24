package com.boomi.flow.external.storage.keys;

import com.google.common.base.Strings;

import javax.inject.Provider;

public class KeyRepositoryProvider implements Provider<KeyRepository> {
    @Override
    public KeyRepository get() {
        var resolver = System.getenv("KEY_RESOLVER");
        if (Strings.isNullOrEmpty(resolver) || "environment_variable".equals(resolver.toLowerCase())) {
            return new KeyRepositoryEnvironment();
        }

        throw new RuntimeException(String.format("The resolver \"%s\" is not supported", resolver));
    }
}
