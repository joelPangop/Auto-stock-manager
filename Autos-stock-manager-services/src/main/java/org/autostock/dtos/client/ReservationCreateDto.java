package org.autostock.dtos.client;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationCreateDto(
        @NotNull Long voitureId,
        LocalDate dateVisite,
        String message
) {}
