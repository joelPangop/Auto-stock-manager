package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DocumentRichDto {
    private Long id;
    private String type;
    private String typeLabel;
    private String nomFichier;
    private String description;
    private BigDecimal montant;
    private LocalDateTime dateUpload;
    private boolean principale;
    private String urlFichier;

    // Voiture
    private Long voitureId;
    private String voitureLabel;   // "Marque Modele Annee"
    private String couleur;
    private String vin;

    // Vendeur (depuis la vente associée à la voiture, si existe)
    private String vendeurNom;
    private String vendeurEmail;

    // Client (depuis la vente, si existe)
    private String clientNom;
    private String clientTelephone;
}
