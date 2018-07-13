package com.boomi.flow.external.storage.jaxrs;

import com.boomi.flow.external.storage.keys.PlatformKeyResolver;
import com.google.common.base.Strings;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static org.jose4j.jwa.AlgorithmConstraints.ConstraintType.WHITELIST;
import static org.jose4j.jws.AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384;

@Provider
public class RequestSignatureFilter implements ContainerRequestFilter {
    private final static Logger LOGGER = LoggerFactory.getLogger(RequestSignatureFilter.class);

    private final PlatformKeyResolver platformKeyResolver;

    @Inject
    public RequestSignatureFilter(PlatformKeyResolver platformKeyResolver) {
        this.platformKeyResolver = platformKeyResolver;
    }

    @Override
    public void filter(ContainerRequestContext context) {
        // We don't want to run this on the health check endpoint
        if (context.getUriInfo().getPath().equals("/health")) {
            return;
        }

        // If we're not given a tenant ID, then we reject the call
        var tenant = context.getUriInfo().getPathParameters().getFirst("tenant");
        if (Strings.isNullOrEmpty(tenant)) {
            LOGGER.warn("Rejecting request because no tenant ID could be determined");

            context.abortWith(Response.status(401).build());
            return;
        }

        // If we're not given a signature, then we reject the call
        var signature = context.getHeaderString("X-ManyWho-Signature");
        if (Strings.isNullOrEmpty(signature)) {
            LOGGER.warn("Rejecting request because no signature header was provided");

            context.abortWith(Response.status(401).build());
            return;
        }

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setMaxFutureValidityInMinutes(5)
                .setRequireSubject()
                .setExpectedIssuer("manywho")
                .setExpectedAudience(tenant)
                .setVerificationKeyResolver(platformKeyResolver)
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(WHITELIST, ECDSA_USING_P384_CURVE_AND_SHA384))
                .build();

        JwtClaims claims;
        try {
            claims = jwtConsumer.processToClaims(signature);
        } catch (InvalidJwtException e) {
            LOGGER.error("Rejecting request because an invalid JWT was given in the signature header", e);

            context.abortWith(Response.status(401).build());
            return;
        }

        if (claims.hasClaim("path") == false) {
            LOGGER.warn("The request signature is missing a claim for the path");

            context.abortWith(Response.status(401).build());
            return;
        }

        String pathClaim;
        try {
            pathClaim = claims.getStringClaimValue("path");
        } catch (MalformedClaimException e) {
            LOGGER.error("The given path claim was malformed", e);

            context.abortWith(Response.status(401).build());
            return;
        }

        if (pathClaim.equals(context.getUriInfo().getRequestUri().getPath()) == false) {
            LOGGER.warn("The request signature's path claim does not match the current path");

            context.abortWith(Response.status(401).build());
            return;
        }
    }
}
