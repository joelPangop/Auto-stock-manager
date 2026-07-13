package org.autostock.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
        @NotBlank @Size(min = 2, max = 120) String nom,
        @Email @NotBlank String email,
        String phoneNumber,
        String role
) {}
