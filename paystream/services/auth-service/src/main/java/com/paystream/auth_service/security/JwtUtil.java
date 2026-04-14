package com.paystream.auth_service.security;

import com.paystream.auth_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.xml.crypto.Data;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.access-token-expiry-ms}") long expiryMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = expiryMs;
    }

    public String generateToken(User user){
        return Jwts.builder()
                .subject(user.getId().toString())
                .claims(Map.of("email",user.getEmail(),"roles", user.getRoles()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+accessTokenExpiryMs))
                .signWith(signingKey)
                .compact();
    }

    //Returns claim if valid ; throws JwtExpection if invalid or expired
    public Claims ValidateAndParse(String token){
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
