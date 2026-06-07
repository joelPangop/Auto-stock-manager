package org.autostock.controllers;

import org.autostock.dtos.*;
import org.autostock.services.DepenseService;
import org.autostock.services.DocumentService;
import org.autostock.services.EntretienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/depenses/{voitureId}")
public class DepenseController {

    @Autowired
    private DepenseService depenseService;

    @Autowired
    private EntretienService entretienService;

    @Autowired
    private DocumentService documentService;

    @PostMapping
    public DepenseDto create(@PathVariable Long voitureId, @RequestBody DepenseCreateDto dto) {
        return depenseService.create(voitureId, dto);
    }

    @GetMapping
    public PageVm<DepenseDto> list(@PathVariable Long voitureId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return depenseService.listByVoiture(voitureId, page, size);
    }

    @GetMapping("/{depenseId}")
    public DepenseDto get(@PathVariable Long voitureId, @PathVariable Long depenseId) {
        return depenseService.getById(voitureId, depenseId);
    }

    @GetMapping("/non-justifiees")
    public PageVm<DepenseDto> nonJustifiees(@PathVariable Long voitureId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return depenseService.listNonJustifiees(voitureId, page, size);
    }

    @GetMapping("/monthly")
    public List<DepenseMonthlyTotalDto> monthly(@PathVariable Long voitureId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return depenseService.monthlyTotals(voitureId, start, end);
    }

    @GetMapping("/monthly-by-categorie")
    public List<DepenseMonthlyByCategorieDto> monthlyByCategorie(@PathVariable Long voitureId,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return depenseService.monthlyTotalsByCategorie(voitureId, start, end);
    }

    @GetMapping("/dashboard")
    public DepenseDashboardVm dashboard(@PathVariable Long voitureId,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(defaultValue = "true") boolean includeMonthly,
                                        @RequestParam(defaultValue = "false") boolean includeMonthlyByCategorie,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return depenseService.dashboard(voitureId, page, size, start, end, includeMonthly, includeMonthlyByCategorie);
    }

    @DeleteMapping("/{depenseId}")
    public void delete(@PathVariable Long voitureId, @PathVariable Long depenseId) {
        depenseService.delete(voitureId, depenseId);

        DepenseDto depenseDto = depenseService.getById(voitureId, depenseId);
        if (depenseDto != null) {
            if (depenseDto.getEntretienId() != null) {
                entretienService.deleteById(depenseDto.getEntretienId());
            }
            if (depenseDto.getDocumentId() != null) {
                documentService.deleteById(depenseDto.getDocumentId());
            }
        }
    }

    @PutMapping("/{depenseId}")
    public DepenseDto update(@PathVariable Long voitureId, @PathVariable Long depenseId, @RequestBody DepenseCreateDto dto) {
        return depenseService.update(voitureId, depenseId, dto);
    }
}
