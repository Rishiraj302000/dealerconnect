package com.dealerconnect.api_gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Set;

/**
 * Coarse, gateway-level role rules derived from the DealerConnect user-story matrix:
 *
 *   - /audit/**             : ADMIN only (audit history is an Admin concern).
 *   - /dealers, /contacts   : reads (GET) for both roles; writes for ADMIN only.
 *   - /favorites/**         : both roles (Relationship Manager owns favorites).
 *
 * Anything else that is authenticated is allowed through by default.
 */
@Component
public class RouteAuthorization {

    private static final String ADMIN = "ADMIN";
    private static final String RELATIONSHIP_MANAGER = "RELATIONSHIP_MANAGER";

    private static final Set<HttpMethod> WRITE_METHODS =
            Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE);

    private final AntPathMatcher matcher = new AntPathMatcher();

    public boolean isAllowed(String path, HttpMethod method, String role) {
        if (role == null) {
            return false;
        }

        if (matcher.match("/audit/**", path)) {
            return ADMIN.equals(role);
        }

        if (matcher.match("/dealers/**", path) || matcher.match("/contacts/**", path)) {
            boolean isWrite = WRITE_METHODS.contains(method);
            return isWrite ? ADMIN.equals(role) : isKnownRole(role);
        }

        if (matcher.match("/favorites/**", path)) {
            return isKnownRole(role);
        }

        return isKnownRole(role);
    }

    private boolean isKnownRole(String role) {
        return ADMIN.equals(role) || RELATIONSHIP_MANAGER.equals(role);
    }
}
