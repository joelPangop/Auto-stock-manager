package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.StockMouvementDto;
import org.autostock.models.StockMouvement;
import org.autostock.models.Voiture;
import org.autostock.repositories.StockMouvementRepository;
import org.autostock.services.StockMouvementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mouvements")
@RequiredArgsConstructor
public class StockMouvementController {

    @Autowired
    private StockMouvementService stockMouvementService;

    @Autowired
    private StockMouvementRepository mouvementRepository;

    @GetMapping("/all")
    @Transactional(readOnly = true)
    public List<StockMouvementDto> listAll() {
        return mouvementRepository.findAllWithVoiture().stream().map(m -> {
            StockMouvementDto dto = new StockMouvementDto();
            dto.setId(m.getId());
            dto.setType(m.getType().getValue());
            dto.setTypeLabel(m.getType().getLabel());
            dto.setDateMouvement(m.getDateMouvement());
            dto.setCommentaire(m.getCommentaire());
            Voiture v = m.getVoiture();
            if (v != null) {
                dto.setVoitureId(v.getId());
                StringBuilder label = new StringBuilder();
                try {
                    if (v.getModele() != null) {
                        if (v.getModele().getMarque() != null) label.append(v.getModele().getMarque().getNom()).append(" ");
                        label.append(v.getModele().getNom()).append(" ");
                    }
                } catch (Exception ignored) {}
                label.append(v.getAnnee());
                dto.setVoitureLabel(label.toString().trim());
                dto.setVoitureStatut(v.getStatut() != null ? v.getStatut().name() : null);
            }
            return dto;
        }).toList();
    }

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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        stockMouvementService.deleteById(id);
    }
}
