package org.autostock.models;
import jakarta.persistence.*;
import lombok.*;
import org.autostock.enums.TypeDocument;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_voiture")
    private Voiture voiture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeDocument type;

    @Column(nullable = false, length = 255)
    private String urlFichier;   // ex : chemin S3, URL, etc.

    @Column(nullable = false)
    private LocalDateTime dateUpload;

    @Column(length = 255)
    private String nomFichier;   // optionnel : nom d’origine

    @Column(columnDefinition = "TEXT")
    private String description;  // notes libres sur le document

    private BigDecimal montant;

    /** Indique si cette photo est la photo principale du véhicule (affichée en couverture). */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean principale = false;
}
