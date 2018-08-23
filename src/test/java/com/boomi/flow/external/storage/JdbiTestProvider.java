package com.boomi.flow.external.storage;

import com.google.inject.Provider;
import com.manywho.sdk.services.utils.Environment;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Inject;

public class JdbiTestProvider implements Provider<Jdbi> {
    private static Jdbi jdbi;

    @Inject
    public JdbiTestProvider() {
    }

    @Override
    public Jdbi get() {
        if (jdbi == null) {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setPassword(Environment.getRequired("DATABASE_PASSWORD"));
            hikariConfig.setUsername(Environment.getRequired("DATABASE_USERNAME"));
            hikariConfig.setJdbcUrl(Environment.getRequired("DATABASE_URL"));
            hikariConfig.setMaximumPoolSize(10);
            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);

            jdbi = Jdbi.create(hikariDataSource);
        }

        return jdbi;
    }
}
