package com.paystream.auth_service.service;

import com.paystream.auth_service.dto.request.LoginRequest;
import com.paystream.auth_service.dto.request.RegisterRequest;
import com.paystream.auth_service.dto.response.AuthResponse;
import com.paystream.auth_service.exception.TokenRevokedException;
import com.paystream.auth_service.model.RefreshToken;
import com.paystream.auth_service.model.User;
import com.paystream.auth_service.repository.RefreshTokenRepository;
import com.paystream.auth_service.repository.UserRepository;
import com.paystream.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository rtRepo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder   passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .name(req.name())
                .roles(List.of("ROLE_USER"))
                .build();
        userRepo.save(user);
        return buildTokenPair(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return buildTokenPair(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        RefreshToken rt = rtRepo.findByTokenHash(hash)
                .orElseThrow(() -> new TokenRevokedException("Refresh token not found"));
        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenRevokedException("Refresh token expired or revoked");
        }
        rt.setRevoked(true);  // rotate: invalidate old token
        rtRepo.save(rt);
        return buildTokenPair(rt.getUser());
    }

    private AuthResponse buildTokenPair(User user) {
        String accessToken = jwtUtil.generateToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256(rawRefreshToken))
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();
        rtRepo.save(rt);
        return new AuthResponse(accessToken, rawRefreshToken, 900);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}


