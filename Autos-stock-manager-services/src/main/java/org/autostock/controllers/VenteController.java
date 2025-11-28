package org.autostock.controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.autostock.dtos.VenteCreateDto;
import org.autostock.dtos.VenteDto;
import org.autostock.mappers.VenteMapper;
import org.autostock.models.Vente;
import org.autostock.services.PaiementService;
import org.autostock.services.VenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
public class VenteController {

    @Autowired
    private VenteService venteService;

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private VenteMapper venteMapper;

    @PostMapping
    public VenteDto create(@RequestBody VenteCreateDto dto) {
        Vente v = venteService.creerVente(
                dto.getIdVoiture(), dto.getIdClient(), dto.getIdVendeur(),
                dto.getPrixFinal(), dto.getModePaiement()
        );
        BigDecimal total = paiementService.totalPaye(v.getId());
        BigDecimal reste = v.getPrixFinal().subtract(total);
        return venteMapper.toDto(v, total, reste);
    }

    @GetMapping("/{id}")
    public VenteDto get(@PathVariable Long id) {
        Vente v = venteService.findById(id).orElseThrow(() -> new EntityNotFoundException("Vente introuvable"));
        BigDecimal total = paiementService.totalPaye(id);
        BigDecimal reste = v.getPrixFinal().subtract(total);
        return venteMapper.toDto(v, total, reste);
    }

    @GetMapping("/client/{idClient}")
    public List<VenteDto> ventesClient(@PathVariable Long idClient) {
        return venteService.ventesDuClient(idClient).stream().map(v -> {
            var total = paiementService.totalPaye(v.getId());
            var reste = v.getPrixFinal().subtract(total);
            return venteMapper.toDto(v, total, reste);
        }).toList();
    }

    @GetMapping("/vendeur/{idVendeur}")
    public List<VenteDto> ventesVendeur(@PathVariable Long idVendeur) {
        return venteService.ventesDuVendeur(idVendeur).stream().map(v -> {
            var total = paiementService.totalPaye(v.getId());
            var reste = v.getPrixFinal().subtract(total);
            return venteMapper.toDto(v, total, reste);
        }).toList();
    }

    @GetMapping("/between")
    public List<VenteDto> ventesEntre(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin
    ) {
        return venteService.ventesEntre(debut, fin).stream().map(v -> {
            var total = paiementService.totalPaye(v.getId());
            var reste = v.getPrixFinal().subtract(total);
            return venteMapper.toDto(v, total, reste);
        }).toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        venteService.deleteById(id);
    }

    @GetMapping("/count")
    public Integer count(){
        return venteService.findAll().size();
    }
}
