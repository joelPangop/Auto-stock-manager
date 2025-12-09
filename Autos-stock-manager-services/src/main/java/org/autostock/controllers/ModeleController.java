package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.ModeleCreateDto;
import org.autostock.dtos.ModeleDto;
import org.autostock.services.MarqueService;
import org.autostock.services.ModeleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/modeles")
@RequiredArgsConstructor
public class ModeleController {

    @Autowired
    public MarqueService  marqueService;

    @Autowired
    public ModeleService  modeleService;

    @GetMapping
    public List<ModeleDto> listByMarque(@RequestParam Long idMarque) {
        return modeleService.listByMarque(idMarque);
    }

    @PostMapping
    public ModeleDto create(@RequestBody ModeleCreateDto dto) throws AccessDeniedException {
        return modeleService.create(dto);
    }
}
