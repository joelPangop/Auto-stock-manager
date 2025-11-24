package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaiementDto {
    private Long id;
    private BigDecimal montant;
    private String methode;
    private String methodeLabel; // "Carte bancaire"...
    private LocalDateTime datePaiement;
}
