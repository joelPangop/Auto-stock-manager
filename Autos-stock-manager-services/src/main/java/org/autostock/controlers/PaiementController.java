package org.autostock.controlers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.PaiementCreateDto;
import org.autostock.dtos.PaiementDto;
import org.autostock.mappers.PaiementMapper;
import org.autostock.models.Paiement;
import org.autostock.services.PaiementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventes/{idVente}/paiements")
@RequiredArgsConstructor
public class PaiementController {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private final PaiementMapper paiementMapper;

    @PostMapping
    public PaiementDto add(@PathVariable Long idVente, @RequestBody PaiementCreateDto dto) {
        Paiement p = paiementService.ajouterPaiement(idVente, dto.getMontant(), dto.getMethode());
        return paiementMapper.toDto(p);
    }

    @GetMapping
    public List<PaiementDto> list(@PathVariable Long idVente) {
        return paiementService.paiementsDeLaVente(idVente).stream()
                .map(paiementMapper::toDto).toList();
    }
}
