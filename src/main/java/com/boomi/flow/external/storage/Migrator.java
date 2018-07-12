package com.boomi.flow.external.storage;

import com.boomi.flow.external.storage.utils.Environment;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import java.net.URI;

public class Migrator {
    public static void executeMigrations() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(Environment.get("DATABASE_URL"));
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setPassword(Environment.get("DATABASE_PASSWORD"));
        hikariConfig.setUsername(Environment.get("DATABASE_USERNAME"));
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);

        // Run the migrations, then destroy the single-connection pool
        Flyway flyway = new Flyway();
        flyway.setDataSource(hikariDataSource);

        // possible supported values for path are mysql, postgresql and sqlserver
        String path = URI.create(Environment.get("DATABASE_URL").trim().substring(5)).getScheme();
        flyway.setLocations(String.format("migrations/%s", path));
        flyway.migrate();

        hikariDataSource.close();
    }
}
