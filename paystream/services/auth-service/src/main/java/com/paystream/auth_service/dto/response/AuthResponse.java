package com.paystream.auth_service.dto.response;


public record AuthResponse(
        String accessToken,
        String refreshToken,
        int expiresInSeconds
) {}
