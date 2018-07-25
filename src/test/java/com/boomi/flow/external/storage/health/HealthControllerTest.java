package com.boomi.flow.external.storage.health;

import com.boomi.flow.external.storage.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

public class HealthControllerTest extends BaseTest {

    @Test
    public void testHealthCheck() {
        String url = testUrl("/health");
        Client client = ClientBuilder.newClient();
        Response response = client.target(url).request().get();
        Assert.assertEquals(200, response.getStatus());
    }
}
