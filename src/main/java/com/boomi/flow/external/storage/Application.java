package com.boomi.flow.external.storage;

import com.boomi.flow.external.storage.utils.Environment;
import com.manywho.sdk.services.servers.EmbeddedServer;
import com.manywho.sdk.services.servers.undertow.UndertowServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

public class Application {
    public static void main(String[] args) throws Exception {
        // Set up a temporary single-connection pool to the database
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(Environment.get("DATABASE_URL"));
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setPassword(Environment.get("DATABASE_PASSWORD"));
        hikariConfig.setUsername(Environment.get("DATABASE_USERNAME"));
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);

        // Run the migrations, then destroy the single-connection pool
        Flyway flyway = new Flyway();
        flyway.setDataSource(hikariDataSource);
        flyway.setLocations(String.format("migrations/%s", Environment.get("DATABASE_TYPE").toLowerCase()));
        flyway.migrate();

        hikariDataSource.close();

        EmbeddedServer server = new UndertowServer();
        server.addModule(new ApplicationModule());
        server.setApplication(Application.class);
        server.start("/api/storage/1");
    }
}
