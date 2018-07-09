package com.boomi.flow.external.storage.guice;

import com.boomi.flow.external.storage.states.*;
import javax.inject.Inject;
import com.boomi.flow.external.storage.utils.Environment;
import com.google.inject.Provider;
import org.jdbi.v3.core.Jdbi;

import java.net.URI;

public class StateRepositoryProvider implements Provider<StateRepository> {
    private final Jdbi jdbi;

    @Inject
    public StateRepositoryProvider(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public StateRepository get() {
        String databaseType = URI.create(Environment.get("DATABASE_URL").substring(5)).getScheme();

        switch (databaseType) {
            case "mysql":
                return new MySqlStateDatabaseRepository(jdbi);
            case "sqlserver":
                return new SqlServerDatabaseRepository(jdbi);
            case "postgresql":
                return new PostgresqlStateDatabaseRepository(jdbi);
        }

        throw new RuntimeException("Database not supported");
    }
}
