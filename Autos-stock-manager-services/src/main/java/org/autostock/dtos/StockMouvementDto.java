package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StockMouvementDto {
    private Long id;
    private String type;
    private String typeLabel;
    private LocalDateTime dateMouvement;
    private String commentaire;
    private Long voitureId;
    private String voitureLabel;
    private String voitureStatut;
}
