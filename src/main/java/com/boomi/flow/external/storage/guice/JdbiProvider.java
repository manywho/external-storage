package com.boomi.flow.external.storage.guice;

import com.google.inject.Provider;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Inject;

public class JdbiProvider implements Provider<Jdbi> {
    private final HikariDataSource hikariDataSource;

    @Inject
    public JdbiProvider(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    @Override
    public Jdbi get() {
        return Jdbi.create(hikariDataSource);
    }
}
