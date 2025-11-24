package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaiementCreateDto {
    private BigDecimal montant;
    private String methode; // "CARD", "CASH", etc.
}
