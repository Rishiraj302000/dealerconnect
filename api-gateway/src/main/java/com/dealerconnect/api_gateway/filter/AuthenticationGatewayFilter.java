package com.dealerconnect.api_gateway.filter;

import com.dealerconnect.api_gateway.security.JwtValidator;
import com.dealerconnect.api_gateway.security.RouteAuthorization;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Centralised authentication/authorization for every request entering the platform.
 * Public auth endpoints pass straight through; all other routes require a valid,
 * unexpired JWT whose role satisfies the {@link RouteAuthorization} rules. The
 * username and role are forwarded downstream as headers so services can trust them
 * without re-validating the token.
 */
@Component
public class AuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of("/auth/login", "/auth/register");
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtValidator jwtValidator;
    private final RouteAuthorization routeAuthorization;

    public AuthenticationGatewayFilter(JwtValidator jwtValidator, RouteAuthorization routeAuthorization) {
        this.jwtValidator = jwtValidator;
        this.routeAuthorization = routeAuthorization;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // CORS preflight requests carry no credentials and must not be authenticated.
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        if (PUBLIC_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return deny(exchange, HttpStatus.UNAUTHORIZED, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        Claims claims;
        try {
            claims = jwtValidator.parse(token);
        } catch (ExpiredJwtException ex) {
            return deny(exchange, HttpStatus.UNAUTHORIZED, "Token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            return deny(exchange, HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        if (!routeAuthorization.isAllowed(path, request.getMethod(), role)) {
            return deny(exchange, HttpStatus.FORBIDDEN, "Role '" + role + "' is not allowed to access this resource");
        }

        ServerHttpRequest mutated = request.mutate()
                .header("X-Auth-User", username)
                .header("X-Auth-Role", role)
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private Mono<Void> deny(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":" + status.value()
                + ",\"error\":\"" + status.getReasonPhrase() + "\""
                + ",\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
