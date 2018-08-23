package com.boomi.flow.external.storage.guice;

import com.boomi.flow.external.storage.jdbi.UuidArgumentFactory;
import com.boomi.flow.external.storage.states.MySqlStateDatabaseRepository;
import com.boomi.flow.external.storage.states.PostgresqlStateDatabaseRepository;
import com.boomi.flow.external.storage.states.SqlServerDatabaseRepository;
import com.boomi.flow.external.storage.states.StateRepository;
import com.google.inject.Provider;
import com.manywho.sdk.services.utils.Environment;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Inject;
import java.net.URI;

public class StateRepositoryProvider implements Provider<StateRepository> {
    private final Jdbi jdbi;

    @Inject
    public StateRepositoryProvider(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public StateRepository get() {
        // Heroku compatibility
        var databaseUrl = Environment.get("JDBC_DATABASE_URL");
        if (databaseUrl == null) {
            databaseUrl = Environment.getRequired("DATABASE_URL");
        }

        String databaseType = URI.create(databaseUrl.substring(5)).getScheme();

        switch (databaseType) {
            case "mysql":
                jdbi.registerArgument(new UuidArgumentFactory());
                return new MySqlStateDatabaseRepository(jdbi);
            case "sqlserver":
                return new SqlServerDatabaseRepository(jdbi);
            case "postgresql":
                return new PostgresqlStateDatabaseRepository(jdbi);
        }

        throw new RuntimeException("Database not supported");
    }
}
