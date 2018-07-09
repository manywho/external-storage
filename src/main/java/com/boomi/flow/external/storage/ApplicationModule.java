package com.boomi.flow.external.storage;

import com.boomi.flow.external.storage.guice.JdbiProvider;
import com.boomi.flow.external.storage.guice.StateRepositoryProvider;
import com.boomi.flow.external.storage.keys.KeyRepository;
import com.boomi.flow.external.storage.keys.KeyRepositoryProvider;
import com.boomi.flow.external.storage.states.StateRepository;
import com.google.inject.AbstractModule;
import org.jdbi.v3.core.Jdbi;

import javax.inject.Singleton;

public class ApplicationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Jdbi.class).toProvider(JdbiProvider.class).in(Singleton.class);
        bind(KeyRepository.class).toProvider(KeyRepositoryProvider.class).in(Singleton.class);
        bind(StateRepository.class).toProvider(StateRepositoryProvider.class).in(Singleton.class);
    }
}
