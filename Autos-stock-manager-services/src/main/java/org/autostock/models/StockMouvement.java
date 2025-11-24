package org.autostock.models;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import org.autostock.enums.TypeMouvement;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMouvement extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_voiture", nullable = false)
    private Voiture voiture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeMouvement type;

    @Column(nullable = false)
    private LocalDateTime dateMouvement;

    @Column(columnDefinition = "TEXT")
    private String commentaire;
}
