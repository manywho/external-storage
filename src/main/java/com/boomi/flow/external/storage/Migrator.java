package com.boomi.flow.external.storage;

import com.boomi.flow.external.storage.guice.HikariDataSourceProvider;
import com.boomi.flow.external.storage.utils.Environment;
import org.flywaydb.core.Flyway;

import java.net.URI;

public class Migrator {
    public static void executeMigrations() {
        var dataSource = new HikariDataSourceProvider().get();

        // Run the migrations, then destroy the single-connection pool
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);

        // possible supported values for path are mysql, postgresql and sqlserver
        String path = URI.create(Environment.get("DATABASE_URL").trim().substring(5)).getScheme();
        flyway.setLocations(String.format("migrations/%s", path));
        flyway.migrate();

        dataSource.close();
    }
}
