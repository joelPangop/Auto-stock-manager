package org.autostock.controlers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.StockMouvementDto;
import org.autostock.services.StockMouvementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voitures/{idVoiture}/mouvements")
@RequiredArgsConstructor
public class StockMouvementController {

    @Autowired
    private StockMouvementService stockMouvementService;

    @GetMapping
    public List<StockMouvementDto> list(@PathVariable Long idVoiture) {
        return stockMouvementService.historiqueVoiture(idVoiture).stream().map(m -> {
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
