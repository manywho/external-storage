package com.boomi.flow.external.storage.health;

import com.boomi.flow.external.storage.BaseTest;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Assert;
import org.junit.Test;
import java.net.URISyntaxException;

public class HealthControllerTest extends BaseTest {
    @Test
    public void testHealthCheck() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.get("/health");
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(200, response.getStatus());
    }
}
