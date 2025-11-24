package org.autostock.mappers;

import org.autostock.dtos.PaiementDto;
import org.autostock.models.Paiement;
import org.springframework.stereotype.Component;

@Component
public class PaiementMapper {

    public PaiementDto toDto(Paiement p) {
        PaiementDto dto = new PaiementDto();
        dto.setId(p.getId());
        dto.setMontant(p.getMontant());
        dto.setDatePaiement(p.getDatePaiement());
        dto.setMethode(p.getMethode().getValue());
        dto.setMethodeLabel(p.getMethode().getLabel());
        return dto;
    }
}
