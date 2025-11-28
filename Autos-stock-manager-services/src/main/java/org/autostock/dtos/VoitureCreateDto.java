package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;
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
    private BigDecimal prixVente;
    private Long kilometrage;
    StatutVoiture statut;
    LocalDate dateEntreeStock;
    Boolean creerMouvementEntree;
    // getters / setters
}
