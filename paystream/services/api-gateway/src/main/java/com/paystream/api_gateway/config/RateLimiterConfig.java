package com.paystream.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    // Rate limit by IP address — used for login/register endpoints
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                "ip:" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest()
                    .getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Extract user ID from JWT subject — simple string split (no full parse needed)
                // In production: parse properly with JwtUtil
                String token = authHeader.substring(7);
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    // Decode payload (base64) and extract sub field
                    String payload = new String(java.util.Base64.getDecoder().decode(parts[1]));
                    // Simple extraction — use a proper JSON parser in production
                    String userId = payload.replaceAll(".*\"sub\":\"([^\"]+)\".*", "$1");
                    return Mono.just("user:" + userId);
                }
            }
            // Fall back to IP if no valid token
            return Mono.just("ip:" + exchange.getRequest()
                    .getRemoteAddress().getAddress().getHostAddress());
        };
    }
}



