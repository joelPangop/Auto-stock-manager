package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DocumentCreateDto {
    private String type;         // "INVOICE", "REGISTRATION"...
    private String urlFichier;
    private LocalDateTime dateUpload;  // optionnel
    private String description;
    private BigDecimal montant;
    // ✅ optionnel : si présent => crée Depense
    private DepenseCreateDto depense;
}
