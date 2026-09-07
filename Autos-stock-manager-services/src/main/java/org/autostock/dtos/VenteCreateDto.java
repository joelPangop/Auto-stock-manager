package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VenteCreateDto {
    private Long idVoiture;
    private Long idClient;
    private Long idVendeur;
    private BigDecimal prixFinal;
    private String modePaiement; // "CASH", "CARD", etc. (MethodePaiement.value)
    // Date saisie dans le formulaire ('YYYY-MM-DD'). Absente du DTO, elle etait
    // ignoree par Jackson et la vente etait systematiquement datee du jour.
    private LocalDate dateVente;

    // getters / setters
}
