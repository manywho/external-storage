package com.boomi.flow.external.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manywho.sdk.api.jackson.ObjectMapperFactory;
import com.manywho.sdk.services.jaxrs.resolvers.ObjectMapperContextResolver;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.junit.BeforeClass;
import javax.ws.rs.Path;


public class BaseTest {
    protected static Dispatcher dispatcher;
    protected static ObjectMapper objectMapper;

    @BeforeClass
    public static void setUp() {
        ApplicationTest application = new ApplicationTest();

        objectMapper = ObjectMapperFactory.create();
        dispatcher = MockDispatcherFactory.createDispatcher();

        for (Class<?> klass : application.getClasses()) {
            dispatcher.getRegistry().addPerRequestResource(klass);
        }

        dispatcher.getProviderFactory().registerProvider(ObjectMapperContextResolver.class);

        for (Object singleton : application.getSingletons()) {
            if (singleton.getClass().isAnnotationPresent(Path.class)) {
                dispatcher.getRegistry().addSingletonResource(singleton);
            } else if (singleton.getClass().getSuperclass().isAnnotationPresent(Path.class)) {
                dispatcher.getRegistry().addSingletonResource(singleton);
            }
        }
    }
}
