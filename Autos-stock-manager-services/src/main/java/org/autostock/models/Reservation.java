package org.autostock.models;

import jakarta.persistence.*;
import lombok.*;
import org.autostock.enums.StatutReservation;

import java.time.LocalDate;

@Entity
@Table(name = "reservation")
@Getter @Setter
@NoArgsConstructor
public class Reservation extends AbstractEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte_client")
    private CompteClient compteClient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_voiture")
    private Voiture voiture;

    /** Date souhaitée pour la visite / essai */
    private LocalDate dateVisite;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutReservation statut = StatutReservation.EN_ATTENTE;
}
