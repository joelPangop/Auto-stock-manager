package org.autostock.models;

import jakarta.persistence.*;
import lombok.*;
import org.autostock.enums.TypeEntretien;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "entretien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entretien extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_voiture")
    private Voiture voiture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeEntretien type;

    @Column(precision = 10, scale = 2)
    private BigDecimal cout;

    @Column(nullable = false)
    private LocalDateTime dateEntretien;

    @Column(length = 150)
    private String garage;

    @Column(columnDefinition = "TEXT")
    private String description; // détails libres sur l’entretien
}
