package com.gp.cloud_gateway.filter;

import com.gp.cloud_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter
        extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouterValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                String authHeader = exchange.getRequest()
                        .getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION);
                // Authorization header missing
                if (authHeader == null || authHeader.isBlank()) {
                    return unauthorized(exchange, "Authorization header is missing");
                }
                // Invalid Authorization format
                if (!authHeader.startsWith("Bearer ")) {
                    return unauthorized(exchange, "Authorization header must start with 'Bearer '");
                }
                String token = authHeader.substring(7);

                try {
                    jwtUtil.validateToken(token);
                } catch (Exception ex) {
                    return unauthorized(
                            exchange,
                            "Invalid or expired token"
                    );
                }
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> unauthorized(
            ServerWebExchange exchange,
            String message
    ) {

        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("""
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "%s"
                }
                """, message);

        return exchange.getResponse().writeWith(
                Mono.just(
                        exchange.getResponse()
                                .bufferFactory()
                                .wrap(body.getBytes(StandardCharsets.UTF_8))
                )
        );
    }

    public static class Config {
    }
}
