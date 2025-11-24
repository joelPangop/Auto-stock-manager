package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VenteCreateDto {
    private Long idVoiture;
    private Long idClient;
    private Long idVendeur;
    private BigDecimal prixFinal;
    private String modePaiement; // "CASH", "CARD", etc. (MethodePaiement.value)

    // getters / setters
}
