package com.boomi.flow.external.storage.migrations;

import com.boomi.flow.external.storage.utils.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseMigrations {

    static public String migrationPath(Connection connection) {
        DatabaseType databaseType;
        try {
             databaseType = DatabaseType.fromString(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new RuntimeException("Error getting connection", e);
        }

        switch (databaseType) {
            case SQL_SERVER:
                return "migrations/sqlserver";
            case POSTGRE_SQL:
                return "migrations/postgresql";
            case MY_SQL:
                return "migrations/mysql";
            default:
                throw new RuntimeException(String.format("Database type \"%s\" no supported - the supported databases are PostgreSQL, Sql Server and MySQL", databaseType));
        }
    }
}
