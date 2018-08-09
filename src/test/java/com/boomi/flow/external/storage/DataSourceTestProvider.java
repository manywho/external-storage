package com.boomi.flow.external.storage;

import com.boomi.flow.external.storage.utils.Environment;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.inject.Provider;

public class DataSourceTestProvider implements Provider<HikariDataSource> {
    @Override
    public HikariDataSource get() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPassword(Environment.get("DATABASE_PASSWORD"));
        hikariConfig.setUsername(Environment.get("DATABASE_USERNAME"));
        hikariConfig.setJdbcUrl(Environment.get("DATABASE_URL"));
        hikariConfig.setMaximumPoolSize(1);

        return new HikariDataSource(hikariConfig);
    }
}
