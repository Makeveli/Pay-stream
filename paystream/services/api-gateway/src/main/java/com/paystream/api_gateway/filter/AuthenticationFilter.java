package com.paystream.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.NameConfig> {

    private final SecretKey signingKey;

    public AuthenticationFilter(@Value("${jwt.secret}") String secret) {
        super(NameConfig.class);
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static class NameConfig {
        // Configuration fields if needed
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                String token = authHeader.substring(7);
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(signingKey).build()
                        .parseClaimsJws(token).getBody();

                // Add user ID to forwarded request headers so downstream services can use it
                var mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Roles", claims.get("roles") != null ? claims.get("roles").toString() : "")
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (ExpiredJwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (JwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        };
    }

}
