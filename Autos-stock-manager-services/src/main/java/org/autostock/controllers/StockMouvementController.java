package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.StockMouvementDto;
import org.autostock.services.StockMouvementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mouvements")
@RequiredArgsConstructor
public class StockMouvementController {

    @Autowired
    private StockMouvementService stockMouvementService;

    @GetMapping("/voiture/{voitureId}")
    public List<StockMouvementDto> list(@PathVariable Long voitureId) {
        return stockMouvementService.historiqueVoiture(voitureId).stream().map(m -> {
            var dto = new StockMouvementDto();
            dto.setId(m.getId());
            dto.setType(m.getType().getValue());
            dto.setTypeLabel(m.getType().getLabel());
            dto.setDateMouvement(m.getDateMouvement());
            dto.setCommentaire(m.getCommentaire());
            return dto;
        }).toList();
    }
}
