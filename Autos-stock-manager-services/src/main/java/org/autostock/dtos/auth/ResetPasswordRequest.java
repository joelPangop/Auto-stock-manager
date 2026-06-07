package org.autostock.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String identifier,
        @NotBlank @Size(min = 6, max = 6) String code,
        @NotBlank @Size(min = 6, max = 100) String newPassword
) {}
