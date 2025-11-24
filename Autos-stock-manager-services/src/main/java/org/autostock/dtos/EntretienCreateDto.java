package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EntretienCreateDto {
    private String type;             // "OIL_CHANGE", "SERVICE", etc.
    private BigDecimal cout;
    private LocalDateTime dateEntretien; // optionnel, sinon now
    private String garage;
    private String commentaire;
}
