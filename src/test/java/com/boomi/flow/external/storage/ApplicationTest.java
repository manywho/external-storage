package com.boomi.flow.external.storage;

import com.manywho.sdk.services.servers.Servlet3Server;

public class ApplicationTest extends Servlet3Server {

    public ApplicationTest() {

        this.addModule(new ApplicationModule());
        this.setApplication(ApplicationTest.class);
        this.start();
    }
}
