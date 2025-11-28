package org.autostock.models;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import org.autostock.enums.TypeMouvement;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_mouvement")
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
    @Column(name = "type", nullable = false, length = 20)
    private TypeMouvement type;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;
}
