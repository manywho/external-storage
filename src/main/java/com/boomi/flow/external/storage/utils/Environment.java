package com.boomi.flow.external.storage.utils;

public class Environment {
    // TODO: Move to the SDK
    public static String get(String name) {
        String variable = System.getenv(name);

        if (variable == null) {
            throw new RuntimeException("No " + name + " environment variable is defined");
        }

        return variable;
    }
}
