package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.PageVm;
import org.autostock.dtos.PaiementCreateDto;
import org.autostock.dtos.PaiementDto;
import org.autostock.mappers.PaiementMapper;
import org.autostock.models.Paiement;
import org.autostock.services.PaiementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private final PaiementMapper paiementMapper;

    @PostMapping("/{idVente}")
    public PaiementDto add(@PathVariable Long idVente, @RequestBody PaiementDto dto) {
        Paiement p = paiementService.ajouterPaiement(idVente, dto.getMontant(), dto.getMethode());
        return paiementMapper.toDto(p);
    }

    @GetMapping("/vente/{idVente}")
    public List<PaiementDto> list(@PathVariable Long idVente) {
        return paiementService.paiementsDeLaVente(idVente).stream()
                .map(paiementMapper::toDto).toList();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PageVm<PaiementDto>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePaiement,desc") String sort,
            @RequestParam(defaultValue = "false") boolean onlyMine
    ) throws AccessDeniedException {
        return ResponseEntity.ok(paiementService.getPage(page, size, sort, onlyMine));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<PaiementDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paiementService.getById(id));
    }

    // DELETE /api/paiements/{id} (ADMIN uniquement)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paiementService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/paiements/{id} (ADMIN uniquement) - optionnel
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PaiementDto> update(@PathVariable Long id, @RequestBody PaiementDto dto) {
        return ResponseEntity.ok(paiementService.update(id, dto));
    }
}
