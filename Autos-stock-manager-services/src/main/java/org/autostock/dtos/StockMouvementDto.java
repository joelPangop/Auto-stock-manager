package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StockMouvementDto {
    private Long id;
    private String type;        // "IN", "OUT", "SALE"...
    private String typeLabel;   // "Entrée en stock", "Vente effectuée"...
    private LocalDateTime dateMouvement;
    private String commentaire;

    // getters / setters
}
