package org.autostock.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "option_voiture")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionVoiture extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "id_voiture")
    private Voiture voiture;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // getters/setters
}
