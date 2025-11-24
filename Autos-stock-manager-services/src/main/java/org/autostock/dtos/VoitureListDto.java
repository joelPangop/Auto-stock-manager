package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VoitureListDto {
    private Long id;
    private String marque;
    private String modele;
    private Integer annee;
    private String couleur;
    private String vin;
    private BigDecimal prixVente;
    private String statut; // "EN_STOCK", "VENDUE", etc.

    // getters / setters
}

