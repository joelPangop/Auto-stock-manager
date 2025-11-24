package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class VoitureCreateDto {
    private Long idModele;
    private Long idFournisseur; // optionnel
    private Integer annee;
    private String couleur;
    private String vin;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;

    // getters / setters
}
