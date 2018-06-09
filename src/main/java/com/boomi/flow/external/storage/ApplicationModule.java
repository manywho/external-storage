package com.boomi.flow.external.storage;

import com.boomi.flow.external.storage.guice.JdbiProvider;
import com.google.inject.AbstractModule;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Singleton;

public class ApplicationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Jdbi.class).toProvider(JdbiProvider.class).in(Singleton.class);
    }
}
