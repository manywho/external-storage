package com.boomi.flow.external.storage.utils;


import com.google.common.base.Strings;

public class DatabaseTypeValidator {
    static public void validate() {
        String databaseType = Environment.get("DATABASE_TYPE").toLowerCase();

        if (Strings.isNullOrEmpty(databaseType)) {
            throw new RuntimeException("The DATABASE_TYPE is mandatory and must not be empty");
        }

        if ("mysql".equals(databaseType) == false &&
                "sqlserver".equals(databaseType) == false &&
                "postgresql".equals(databaseType) == false) {

            throw new RuntimeException(String.format("Database type \"%s\" no supported - the supported databases are postgresql, sqlserver and mysql", System.getenv("DATABASE_TYPE")));
        }
    }
}
