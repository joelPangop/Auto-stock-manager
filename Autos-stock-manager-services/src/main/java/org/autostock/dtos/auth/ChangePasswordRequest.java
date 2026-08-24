package org.autostock.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Changement de mot de passe par l'utilisateur lui-même, une fois connecté.
 * Le mot de passe actuel est exigé : sans lui, un jeton volé suffirait à
 * verrouiller le compte de sa victime.
 */
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 6, max = 100) String newPassword
) {}
