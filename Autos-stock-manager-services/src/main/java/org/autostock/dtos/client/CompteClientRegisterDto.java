package org.autostock.dtos.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompteClientRegisterDto(
        @NotBlank String nom,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6) String motDePasse,
        String telephone
) {}
