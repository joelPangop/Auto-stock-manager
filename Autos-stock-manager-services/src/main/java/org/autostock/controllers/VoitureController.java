package org.autostock.controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.autostock.dtos.*;
import org.autostock.enums.StatutVoiture;
import org.autostock.mappers.VoitureMapper;
import org.autostock.models.Fournisseur;
import org.autostock.models.Modele;
import org.autostock.models.Voiture;
import org.autostock.repositories.FournisseurRepository;
import org.autostock.repositories.ModeleRepository;
import org.autostock.services.StockMouvementService;
import org.autostock.services.VoitureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voitures")
@RequiredArgsConstructor
public class VoitureController {

    @Autowired
    private VoitureService voitureService;

    @Autowired
    private VoitureMapper voitureMapper;

    @Autowired
    private final ModeleRepository modeleRepository;

    @Autowired
    private final FournisseurRepository fournisseurRepository;

    @Autowired
    private final StockMouvementService stockMouvementService;

    @PostMapping
    public VoitureDetailDto create(@RequestBody VoitureCreateDto dto) {
        Modele modele = modeleRepository.findById(dto.getIdModele())
                .orElseThrow(() -> new EntityNotFoundException("Modèle introuvable"));
        Fournisseur fournisseur = (dto.getIdFournisseur() == null) ? null :
                fournisseurRepository.findById(dto.getIdFournisseur())
                        .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable"));

        Voiture saved = voitureService.create(voitureMapper.toEntity(dto, modele, fournisseur));
        return voitureMapper.toDetailDto(saved);
    }

    @GetMapping("/{id}")
    public VoitureDetailDto get(@PathVariable Long id) {
        Voiture v = voitureService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));
        return voitureMapper.toDetailDto(v);
    }

    @GetMapping("/count")
    public Integer count(){
        return voitureService.findAll().size();
    }

    @GetMapping
    public List<VoitureListDto> list(@RequestParam(required = false) String marque,
                                     @RequestParam(required = false) StatutVoiture statut) {
        List<Voiture> list;
        if (marque != null && !marque.isBlank() && statut != null) {
            list = voitureService.listerVoituresParMarqueEtStatut(marque, statut);
        } else {
            list = voitureService.findAll();
        }
        return list.stream().map(voitureMapper::toListDto).toList();
    }

    @GetMapping("/en-stock")
    public List<VoitureListDto> enStock() {
        return voitureService.listerVoituresEnStock().stream()
                .map(voitureMapper::toListDto).toList();
    }

    @PostMapping("/{id}/reserver")
    public VoitureDetailDto reserver(@PathVariable Long id) {
        return voitureMapper.toDetailDto(voitureService.reserverVoiture(id));
    }

    @PostMapping("/{id}/liberer")
    public VoitureDetailDto liberer(@PathVariable Long id) {
        return voitureMapper.toDetailDto(voitureService.libererReservation(id));
    }

    @PostMapping("/{id}/vendue")
    public VoitureDetailDto marquerVendue(@PathVariable Long id) {
        return voitureMapper.toDetailDto(voitureService.marquerVendue(id));
    }

    @GetMapping("/{id}/mouvements")
    public List<StockMouvementDto> mouvements(@PathVariable Long id) {
        return stockMouvementService.historiqueVoiture(id).stream()
                .map(m -> {
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
        voitureService.deleteById(id);
    }

    @PutMapping("/{id}")
    public VoitureDetailDto update(@PathVariable Long id, @RequestBody VoitureUpdateDto dto) {

        Modele modele = modeleRepository.findById(dto.getIdModele())
                .orElseThrow(() -> new EntityNotFoundException("Modèle introuvable"));
        Fournisseur fournisseur = (dto.getIdFournisseur() == null) ? null :
                fournisseurRepository.findById(dto.getIdFournisseur())
                        .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable"));

//        Voiture saved = voitureService.create(voitureMapper.toEntity(dto, modele, fournisseur));
        Voiture updated = voitureService.update(id, voitureMapper.toUpdatedEntity(dto, modele, fournisseur));
        return voitureMapper.toDetailDto(updated);
    }

}
