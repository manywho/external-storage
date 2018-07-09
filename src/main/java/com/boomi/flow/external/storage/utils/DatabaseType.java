package com.boomi.flow.external.storage.utils;

public enum DatabaseType {
    MY_SQL("MySQL"),
    SQL_SERVER("Microsoft SQL Server"),
    POSTGRE_SQL("PostgreSQL");

    private String type;

    DatabaseType(String type) {
        this.type = type;
    }

    public static DatabaseType fromString(String productName) {
        for (DatabaseType typeDatabase : DatabaseType.values()) {
            if (typeDatabase.type.equalsIgnoreCase(productName)) {
                return typeDatabase;
            }
        }

        throw new RuntimeException(String.format("Database '%s' not supported", productName));
    }
}