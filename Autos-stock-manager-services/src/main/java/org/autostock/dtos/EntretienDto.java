package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EntretienDto {
    private Long id;
    private String type;
    private String typeLabel;
    private BigDecimal cout;
    private Long idVoiture;
    private String voitureLabel;
    private LocalDateTime dateEntretien;
    private String garage;
    private String commentaire;
    // ✅ optionnel : si présent => crée Depense
    private DepenseCreateDto depense;

}
