package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VoitureUpdateDto {
    Long idMarque;
    Long idModele;
    Integer annee;
    /** Prix affiche sur la vitrine. prixVente n'est plus modifiable ici. */
    BigDecimal prixDemande;
    BigDecimal prixAchat;
    String vin;
    String couleur;
    Long kilometrage;
    String statut;
    String categorie;
    Long idFournisseur;
    private boolean needsRemark;
    private String description;
}
