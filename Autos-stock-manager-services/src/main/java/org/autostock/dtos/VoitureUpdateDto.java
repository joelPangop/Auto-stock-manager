package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VoitureUpdateDto {
    Long iMarque;
    Long idModele;
    Integer annee;
    BigDecimal prixVente;
    BigDecimal prixAchat;
    String vin;
    String couleur;
    Long kilometrage;
    String statut;       // ou enum StatutVoiture
    Long idFournisseur;
}
