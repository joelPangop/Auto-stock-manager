package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.FournisseurDto;
import org.autostock.services.FournisseurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    @Autowired
    private FournisseurService fournisseurService;

    @GetMapping
    public ResponseEntity<List<FournisseurDto>> getAllFournisseurs() {
        List<FournisseurDto> fournisseurs = fournisseurService.getAllFournisseurs();
        return ResponseEntity.ok(fournisseurs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FournisseurDto> getFournisseurById(@PathVariable Long id) {
        FournisseurDto fournisseur = fournisseurService.getFournisseurById(id);
        return ResponseEntity.ok(fournisseur);
    }

    @PostMapping
    public ResponseEntity<FournisseurDto> createFournisseur(@RequestBody FournisseurDto fournisseurDto) throws AccessDeniedException {
        FournisseurDto createdFournisseur = fournisseurService.createFournisseur(fournisseurDto);
        return ResponseEntity.ok(createdFournisseur);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FournisseurDto> updateFournisseur(
            @PathVariable Long id,
            @RequestBody FournisseurDto fournisseurDto) throws AccessDeniedException {
        FournisseurDto updatedFournisseur = fournisseurService.updateFournisseur(id, fournisseurDto);
        return ResponseEntity.ok(updatedFournisseur);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFournisseur(@PathVariable Long id) {
        fournisseurService.deleteFournisseur(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FournisseurDto>> searchByNom(@RequestParam String nom) {
        List<FournisseurDto> fournisseurs = fournisseurService.searchByNom(nom);
        return ResponseEntity.ok(fournisseurs);
    }
}
