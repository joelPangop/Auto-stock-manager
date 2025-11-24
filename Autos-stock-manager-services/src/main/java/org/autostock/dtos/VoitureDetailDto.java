package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class VoitureDetailDto {
    private Long id;
    private String marque;
    private String modele;
    private Long idModele;
    private Long idFournisseur;
    private Integer annee;
    private String couleur;
    private String vin;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private String statut;
    private LocalDateTime dateEntreeStock;
}
