package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;
import org.autostock.enums.CategorieVoiture;
import org.autostock.enums.StatutVoiture;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class VoitureCreateDto {
    private Long idModele;
    private Long idFournisseur; // optionnel
    private Integer annee;
    private String couleur;
    private String vin;
    private BigDecimal prixAchat;
    /** Prix affiche sur la vitrine. Le prix de vente reel vient de la vente. */
    private BigDecimal prixDemande;
    private Long kilometrage;
    StatutVoiture statut;
    CategorieVoiture categorie;
    LocalDate dateEntreeStock;
    Boolean creerMouvementEntree;
    // getters / setters
}
