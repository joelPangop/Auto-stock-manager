package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.*;
import org.autostock.enums.CategorieDepense;
import org.autostock.mappers.DepenseMapper;
import org.autostock.mappers.EntretienMapper;
import org.autostock.models.Entretien;
import org.autostock.models.Voiture;
import org.autostock.services.DepenseService;
import org.autostock.services.EntretienService;
import org.autostock.services.VoitureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entretiens")
@RequiredArgsConstructor
public class EntretienController {

    @Autowired
    private EntretienService entretienService;

    @Autowired
    private EntretienMapper entretienMapper;

    @Autowired
    private DepenseService depenseService;

    @Autowired
    private DepenseMapper depenseMapper;

    @Autowired
    VoitureService voitureService;

    @PostMapping("/{idVoiture}")
    public EntretienDto add(@PathVariable Long idVoiture, @RequestBody EntretienCreateDto dto) {
        Entretien e = entretienService.ajouterEntretien(idVoiture, entretienMapper.toEntity(dto));
        Optional<Voiture> voiture = voitureService.findById(idVoiture);
        // ✅ création automatique de la dépense si dto.depense est fourni (ou si montant présent)

            DepenseCreateDto dDto = new DepenseCreateDto();
            dDto.setCategorie(CategorieDepense.ENTRETIEN);
            dDto.setDescription(dto.getCommentaire());
            dDto.setMontant(dto.getCout());
            dDto.setDateDepense(dto.getDateEntretien());
            dDto.setEntretienId(e.getId());
            // Force la voiture (sécurité)
            dDto.setVoitureId(voiture.get().getId());

            depenseService.create(idVoiture, dDto);
        return entretienMapper.toDto(e);
    }

    @GetMapping("/voiture/{voitureId}")
    public List<EntretienDto> list(@PathVariable Long voitureId) {
        return entretienService.entretiensVoiture(voitureId).stream()
                .map(entretienMapper::toDto).toList();
    }

    @PutMapping("/{entretienId}")
    public EntretienDto update(@PathVariable Long entretienId, @RequestBody EntretienCreateDto dto) {
        // 1. Mettre à jour l'entretien
        Entretien updated = entretienService.modifierEntretien(entretienId, entretienMapper.toEntity(dto));

        // 2. Mettre à jour la dépense associée (supprimer l'ancienne + recréer)
        try {
            depenseService.deleteByEntretienId(entretienId);
            Long voitureId = updated.getVoiture() != null ? updated.getVoiture().getId() : null;
            if (voitureId != null) {
                DepenseCreateDto dDto = new DepenseCreateDto();
                dDto.setCategorie(CategorieDepense.ENTRETIEN);
                dDto.setDescription(dto.getCommentaire());
                dDto.setMontant(dto.getCout());
                dDto.setDateDepense(dto.getDateEntretien());
                dDto.setEntretienId(entretienId);
                dDto.setVoitureId(voitureId);
                depenseService.create(voitureId, dDto);
            }
        } catch (Exception e) {
            // Ne pas bloquer la mise à jour de l'entretien si la dépense échoue
        }

        return entretienMapper.toDto(updated);
    }

    @DeleteMapping("/{entretienId}")
    public void deleteById(@PathVariable Long entretienId) {
        depenseService.deleteByEntretienId(entretienId);
        entretienService.deleteById(entretienId);

    }

    @GetMapping
    public ResponseEntity<PageVm<EntretienDto>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateEntretien,desc") String sort,
            @RequestParam(defaultValue = "false") boolean onlyMine
    ) {
        PageVm<EntretienDto> vm = entretienService.getPage(page, size, sort, onlyMine);
        return ResponseEntity.ok(vm);
    }
}
