package com.boomi.flow.external.storage.guice;

import com.boomi.flow.external.storage.utils.Environment;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

public class JdbiProvider implements Provider<Jdbi> {
    @Override
    public Jdbi get() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPassword(Environment.get("DATABASE_PASSWORD"));
        hikariConfig.setUsername(Environment.get("DATABASE_USERNAME"));
        hikariConfig.setJdbcUrl(Environment.get("DATABASE_URL"));

        return Jdbi.create(new HikariDataSource(hikariConfig));
    }
}
