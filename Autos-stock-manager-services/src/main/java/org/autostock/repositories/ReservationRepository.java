package org.autostock.repositories;

import org.autostock.enums.StatutReservation;
import org.autostock.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCompteClient_IdOrderByCreatedAtDesc(Long clientId);
    boolean existsByCompteClient_IdAndVoiture_IdAndStatutNot(Long clientId, Long voitureId, StatutReservation statut);
}
