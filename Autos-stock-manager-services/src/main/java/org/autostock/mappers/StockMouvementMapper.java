package org.autostock.mappers;

import org.autostock.dtos.StockMouvementDto;
import org.autostock.models.StockMouvement;
import org.springframework.stereotype.Component;

@Component
public class StockMouvementMapper {

    public StockMouvementDto toDto(StockMouvement m) {
        StockMouvementDto dto = new StockMouvementDto();
        dto.setId(m.getId());
        dto.setType(m.getType().getValue());
        dto.setTypeLabel(m.getType().getLabel());
        dto.setDateMouvement(m.getDateMouvement());
        dto.setCommentaire(m.getCommentaire());
        return dto;
    }
}
