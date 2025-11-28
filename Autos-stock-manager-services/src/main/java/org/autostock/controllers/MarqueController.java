package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.MarqueCreateDto;
import org.autostock.dtos.MarqueDto;
import org.autostock.models.Marque;
import org.autostock.repositories.MarqueRepository;
import org.autostock.services.MarqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marques")
@RequiredArgsConstructor
public class MarqueController {

    @Autowired
    private MarqueService marqueService;

    @GetMapping
    public List<MarqueDto> list() {
        return marqueService.list();
    }

    @PostMapping
    public MarqueDto create(@RequestBody MarqueCreateDto dto) {
        return marqueService.create(dto);
    }
}
