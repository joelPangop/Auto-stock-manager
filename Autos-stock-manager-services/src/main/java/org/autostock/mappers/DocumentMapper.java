package org.autostock.mappers;

import org.autostock.dtos.DocumentCreateDto;
import org.autostock.dtos.DocumentDto;
import org.autostock.enums.TypeDocument;
import org.autostock.models.Document;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DocumentMapper {

    public Document toEntity(DocumentCreateDto dto) {
        Document d = new Document();
        d.setType(TypeDocument.fromValue(dto.getType()));
        d.setUrlFichier(dto.getUrlFichier());
        d.setDateUpload(dto.getDateUpload() != null ? dto.getDateUpload() : LocalDateTime.now());
        d.setDescription(dto.getDescription());
        return d;
    }

    public DocumentDto toDto(Document d) {
        DocumentDto dto = new DocumentDto();
        dto.setId(d.getId());
        dto.setType(d.getType().getValue());
        dto.setTypeLabel(d.getType().getLabel());
        dto.setUrlFichier(d.getUrlFichier());
        dto.setDateUpload(d.getDateUpload());
        dto.setDescription(d.getDescription());
        return dto;
    }
}
