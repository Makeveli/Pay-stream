package com.paystream.auth_service.repository;


import com.paystream.auth_service.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;
import java.util.UUID;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {


    // Find a token by its SHA-256 hash (used during refresh)
    Optional<RefreshToken> findByTokenHash(String tokenHash);


    // Revoke all tokens for a user (used on logout or password change)
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(UUID userId);


}


