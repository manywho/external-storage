package com.boomi.flow.external.storage.jaxrs;

import com.google.common.base.Strings;
import com.manywho.sdk.api.run.ServiceProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class RequestValidationFilter implements ContainerRequestFilter {
    private final static Logger LOGGER = LoggerFactory.getLogger(RequestValidationFilter.class);

    private final static String HEADER_PLATFORM_KEY = "X-ManyWho-Platform-Key-ID";
    private final static String HEADER_RECEIVER_KEY = "X-ManyWho-Receiver-Key-ID";

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        // We don't want to run this on the health check endpoint
        if (context.getUriInfo().getPath().equals("/health")) {
            return;
        }

        var platformKeyHeader = context.getHeaderString(HEADER_PLATFORM_KEY);
        if (Strings.isNullOrEmpty(platformKeyHeader)) {
            LOGGER.error("No value for the {} header was given", HEADER_PLATFORM_KEY);

            context.abortWith(Response.status(400)
                    .entity(createServiceProblem(context, "No " + HEADER_PLATFORM_KEY + " header was given"))
                    .build());
            return;
        }

        var receiverKeyHeader = context.getHeaderString(HEADER_RECEIVER_KEY);
        if (Strings.isNullOrEmpty(receiverKeyHeader)) {
            LOGGER.error("No value for the {} header was given", HEADER_RECEIVER_KEY);

            context.abortWith(Response.status(400)
                    .entity(createServiceProblem(context, "No " + HEADER_RECEIVER_KEY + " header was given"))
                    .build());
            return;
        }
    }

    private static ServiceProblem createServiceProblem(ContainerRequestContext context, String message) {
        return new ServiceProblem(
                context.getUriInfo().getRequestUri().getPath(),
                400,
                message
        );
    }
}
