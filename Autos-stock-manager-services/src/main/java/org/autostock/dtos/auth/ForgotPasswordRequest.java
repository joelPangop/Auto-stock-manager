package org.autostock.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.autostock.enums.DeliveryMethod;

public record ForgotPasswordRequest(
        @NotBlank String identifier,
        @NotNull DeliveryMethod deliveryMethod
) {}
