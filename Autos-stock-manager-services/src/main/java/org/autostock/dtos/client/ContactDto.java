package org.autostock.dtos.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactDto(
        @NotBlank String nom,
        @Email @NotBlank String email,
        String telephone,
        @NotBlank String sujet,
        @NotBlank String message
) {}
