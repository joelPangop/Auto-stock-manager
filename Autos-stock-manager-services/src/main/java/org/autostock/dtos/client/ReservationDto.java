package org.autostock.dtos.client;

import org.autostock.enums.StatutReservation;
import org.autostock.models.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationDto(
        Long id,
        Long voitureId,
        String voitureLabel,
        LocalDate dateVisite,
        String message,
        StatutReservation statut,
        LocalDateTime createdAt
) {
    public static ReservationDto from(Reservation r) {
        var v = r.getVoiture();
        String label = v.getModele().getMarque().getNom() + " " + v.getModele().getNom()
                + " (" + v.getAnnee() + ")";
        return new ReservationDto(
                r.getId(),
                v.getId(),
                label,
                r.getDateVisite(),
                r.getMessage(),
                r.getStatut(),
                r.getCreatedAt()
        );
    }
}
