package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autostock.enums.CategorieDepense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "depense")
@Getter
@Setter
public class Depense extends AbstractEntity {

    // Toujours lié à une voiture
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voiture_id", nullable = false)
    private Voiture voiture;

    // Optionnel : lié à un entretien
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entretien_id")
    private Entretien entretien;

    // Optionnel : lié à un document (ex: facture, assurance, amende, etc.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", nullable = false, length = 30)
    private CategorieDepense categorie;

    @Column(name = "montant", nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_depense", nullable = false)
    private LocalDateTime dateDepense;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;
}
