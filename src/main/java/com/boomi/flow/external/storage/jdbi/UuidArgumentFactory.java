package com.boomi.flow.external.storage.jdbi;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;
import java.util.UUID;

/**
 * This argument is used so we can pass directly uuid to MySql
 */
public class UuidArgumentFactory extends AbstractArgumentFactory<UUID> {
        public UuidArgumentFactory() {
            super(Types.VARCHAR);
        }

    @Override
    protected Argument build(UUID value, ConfigRegistry config) {
        return (position, statement, ctx) -> statement.setString(position, value.toString());
    }
    }
