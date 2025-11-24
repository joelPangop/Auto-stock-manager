package org.autostock.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.autostock.enums.MethodePaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiement")
@Getter
@Setter
public class Paiement extends AbstractEntity {
    @ManyToOne
    @JoinColumn(name = "id_vente", nullable = false)
    private Vente vente;

    @Column(nullable = false)
    private BigDecimal montant;

    @Column(nullable = false)
    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MethodePaiement methode; // ou Enum
}
