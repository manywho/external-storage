package com.boomi.flow.external.storage.guice;

import com.boomi.flow.external.storage.utils.Environment;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.inject.Provider;

public class HikariDataSourceProvider implements Provider<HikariDataSource> {
    @Override
    public HikariDataSource get() {
        HikariConfig hikariConfig = new HikariConfig();

        // We do this to support Heroku out of the box
        var jdbcDatabaseUrl = System.getenv("JDBC_DATABASE_URL");
        if (jdbcDatabaseUrl != null) {
            hikariConfig.setJdbcUrl(System.getenv("JDBC_DATABASE_URL"));
        } else {
            hikariConfig.setPassword(Environment.get("DATABASE_PASSWORD"));
            hikariConfig.setUsername(Environment.get("DATABASE_USERNAME"));
            hikariConfig.setJdbcUrl(Environment.get("DATABASE_URL"));
        }

        return new HikariDataSource(hikariConfig);
    }
}
