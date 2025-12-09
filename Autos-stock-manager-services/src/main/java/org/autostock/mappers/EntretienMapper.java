package org.autostock.mappers;

import org.autostock.dtos.EntretienCreateDto;
import org.autostock.dtos.EntretienDto;
import org.autostock.enums.TypeEntretien;
import org.autostock.models.Entretien;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EntretienMapper {

    public Entretien toEntity(EntretienCreateDto dto) {
        Entretien e = new Entretien();
        e.setType(TypeEntretien.fromValue(dto.getType()));
        e.setCout(dto.getCout());
        e.setDateEntretien(dto.getDateEntretien() != null ? dto.getDateEntretien() : LocalDateTime.now());
        e.setGarage(dto.getGarage());
        e.setDescription(dto.getCommentaire());
        return e;
    }

    public EntretienDto toDto(Entretien e) {
        EntretienDto dto = new EntretienDto();
        dto.setId(e.getId());
        dto.setType(e.getType().getValue());
        dto.setTypeLabel(e.getType().getLabel());
        dto.setCout(e.getCout());
        dto.setDateEntretien(e.getDateEntretien());
        dto.setGarage(e.getGarage());
        dto.setCommentaire(e.getDescription());
        return dto;
    }
}
