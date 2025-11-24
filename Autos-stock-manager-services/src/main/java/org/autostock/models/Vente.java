package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "vente")
@Getter
@Setter
public class Vente extends AbstractEntity {
    @OneToOne
    @JoinColumn(name = "id_voiture", nullable = false, unique = true)
    private Voiture voiture;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "id_vendeur", nullable = false)
    private User vendeur;

    @Column(nullable = false)
    private LocalDateTime dateVente;

    @Column(nullable = false)
    private BigDecimal prixFinal;

    @Column(nullable = false)
    private String modePaiement; // ou Enum

    @OneToMany(mappedBy = "vente")
    private List<Paiement> paiements = new ArrayList<>();
}
