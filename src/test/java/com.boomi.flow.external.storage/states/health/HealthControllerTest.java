package com.boomi.flow.external.storage.states.health;

import com.boomi.flow.external.storage.states.BaseTest;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;

public class HealthControllerTest extends BaseTest {
    @Test
    public void testHealthCheck() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.get("/health")
                .accept(MediaType.TEXT_HTML_TYPE)
                .contentType(MediaType.TEXT_HTML_TYPE);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(200, response.getStatus());
    }
}
