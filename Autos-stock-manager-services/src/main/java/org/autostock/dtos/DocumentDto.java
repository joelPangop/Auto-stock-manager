package org.autostock.dtos;

import lombok.Getter;
import lombok.Setter;

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
}
