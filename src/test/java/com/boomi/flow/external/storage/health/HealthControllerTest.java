package com.boomi.flow.external.storage.health;

import com.boomi.flow.external.storage.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

public class HealthControllerTest extends BaseTest {

    @Test
    public void testHealthCheck() {
        String url = testUrl("/health");
        Response response = client.target(url).request().get();
        Assertions.assertEquals(200, response.getStatus());
        response.close();
    }
}
