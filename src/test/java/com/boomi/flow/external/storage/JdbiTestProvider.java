package com.boomi.flow.external.storage;

import com.boomi.flow.external.storage.utils.Environment;
import com.google.inject.Provider;
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
            hikariConfig.setPassword(Environment.get("DATABASE_PASSWORD"));
            hikariConfig.setUsername(Environment.get("DATABASE_USERNAME"));
            hikariConfig.setJdbcUrl(Environment.get("DATABASE_URL"));
            hikariConfig.setMaximumPoolSize(10);
            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);

            jdbi = Jdbi.create(hikariDataSource);
        }

        return jdbi;
    }
}
