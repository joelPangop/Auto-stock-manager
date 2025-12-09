package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.EntretienCreateDto;
import org.autostock.dtos.EntretienDto;
import org.autostock.mappers.EntretienMapper;
import org.autostock.models.Entretien;
import org.autostock.services.EntretienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entretiens")
@RequiredArgsConstructor
public class EntretienController {

    @Autowired
    private EntretienService entretienService;

    @Autowired
    private EntretienMapper entretienMapper;

    @PostMapping("/{idVoiture}")
    public EntretienDto add(@PathVariable Long idVoiture, @RequestBody EntretienCreateDto dto) {
        Entretien e = entretienService.ajouterEntretien(idVoiture, entretienMapper.toEntity(dto));
        return entretienMapper.toDto(e);
    }

    @GetMapping("/voiture/{voitureId}")
    public List<EntretienDto> list(@PathVariable Long voitureId) {
        return entretienService.entretiensVoiture(voitureId).stream()
                .map(entretienMapper::toDto).toList();
    }
}
