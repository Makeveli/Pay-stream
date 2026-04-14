package com.paystream.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


// Java record --- immutable DTO, getters auto-generated
public record RegisterRequest(
        @Email(message = "Must be a valid email")
        @NotBlank
        String email,


        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,


        @NotBlank
        String name
) {}


