package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DocumentCreateDto {
    private String type;         // "INVOICE", "REGISTRATION"...
    private String urlFichier;
    private LocalDateTime dateUpload;  // optionnel
    private String description;
}
