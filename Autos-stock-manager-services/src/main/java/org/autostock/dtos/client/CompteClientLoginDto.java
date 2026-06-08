package org.autostock.dtos.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompteClientLoginDto(
        @Email @NotBlank String email,
        @NotBlank String motDePasse
) {}
