package com.boomi.flow.external.storage.guice;

import com.boomi.flow.external.storage.states.*;
import javax.inject.Inject;
import com.google.inject.Provider;
import org.jdbi.v3.core.Jdbi;

public class StateRepositoryProvider implements Provider<StateRepository> {
    private final Jdbi jdbi;

    @Inject
    public StateRepositoryProvider(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public StateRepository get() {
        switch (System.getenv("DATABASE_TYPE").toLowerCase()) {
            case "mysql":
                return new MySqlStateRepository(jdbi);
            case "sqlserver":
                return new SqlServerRepository(jdbi);
            case "postgresql":
                return new PostgresqlStateRepository(jdbi);
            default:
                throw new RuntimeException("Database type no supported");
        }
    }
}
