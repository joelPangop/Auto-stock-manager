package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DocumentDto {
    private Long id;
    private String type;
    private String typeLabel;
    private String urlFichier;
    private LocalDateTime dateUpload;
    private String description;
    private BigDecimal montant;
    private String nomFichier;
    private boolean principale;
}
