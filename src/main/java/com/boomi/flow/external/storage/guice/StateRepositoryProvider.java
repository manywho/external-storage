package com.boomi.flow.external.storage.guice;

import com.boomi.flow.external.storage.states.*;
import javax.inject.Inject;
import com.boomi.flow.external.storage.utils.Environment;
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
        String databaseType = Environment.get("DATABASE_TYPE").toLowerCase();

        switch (databaseType) {
            case "mysql":
                return new MySqlStateRepository(jdbi);
            case "sqlserver":
                return new SqlServerRepository(jdbi);
            default:
                return new PostgresqlStateRepository(jdbi);
        }
    }
}
