package org.autostock.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EntretienCreateDto {
    private String type;             // "OIL_CHANGE", "SERVICE", etc.
    private BigDecimal cout;

    @JsonFormat(pattern = "yyyy-MM-dd['T'HH:mm[:ss]]")
    private LocalDateTime dateEntretien; // optionnel, sinon now

    private String garage;
    private String commentaire;
}
