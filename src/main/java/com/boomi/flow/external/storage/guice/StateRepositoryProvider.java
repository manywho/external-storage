package com.boomi.flow.external.storage.guice;

import com.boomi.flow.external.storage.states.*;
import javax.inject.Inject;
import com.boomi.flow.external.storage.utils.DatabaseType;
import com.google.inject.Provider;
import org.jdbi.v3.core.Jdbi;
import java.sql.SQLException;

public class StateRepositoryProvider implements Provider<StateRepository> {
    private final Jdbi jdbi;

    @Inject
    public StateRepositoryProvider(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public StateRepository get() {
        String productName;

        try {
            productName = jdbi.withHandle(handle -> handle.getConnection().getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new RuntimeException("Error getting product name", e);
        }

        switch (DatabaseType.fromString(productName)) {
            case MY_SQL:
                return new MySqlStateRepository(jdbi);
            case SQL_SERVER:
                return new SqlServerRepository(jdbi);
            case POSTGRE_SQL:
                return new PostgresqlStateRepository(jdbi);
        }

        throw new RuntimeException("Database not supported");
    }
}
