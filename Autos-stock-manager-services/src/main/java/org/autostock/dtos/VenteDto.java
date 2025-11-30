package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class VenteDto {
    private Long id;
    private Long idVoiture;
    private String vin;
    private String marque;
    private String modele;

    private Long idClient;
    private String nomClient;

    private Long idVendeur;
    private String nomVendeur;

    private LocalDateTime dateVente;
    private BigDecimal prixVente;
    private String modePaiement;

    private BigDecimal prixFinal;
    private BigDecimal resteAPayer;

    // getters / setters
}
