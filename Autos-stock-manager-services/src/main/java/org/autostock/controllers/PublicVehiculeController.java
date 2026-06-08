package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.client.VoiturePublicDetailDto;
import org.autostock.dtos.client.VoiturePublicDto;
import org.autostock.enums.StatutVoiture;
import org.autostock.enums.TypeDocument;
import org.autostock.models.Document;
import org.autostock.repositories.DocumentRepository;
import org.autostock.repositories.MarqueRepository;
import org.autostock.repositories.VoitureRepository;
import org.autostock.services.DocumentServiceImpl;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Endpoints publics (sans authentification) pour le portail client Ted Auto.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicVehiculeController {

    private final VoitureRepository voitureRepo;
    private final DocumentRepository documentRepo;
    private final MarqueRepository marqueRepo;
    private final DocumentServiceImpl documentService;

    /** Liste paginée des véhicules disponibles (EN_STOCK ou RESERVEE). */
    @GetMapping("/vehicules")
    public ResponseEntity<?> catalogue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Integer anneeMin
    ) {
        // Récupérer tous les véhicules disponibles et filtrer en mémoire
        // (volume faible en début de projet, facile à migrer en JPQL si nécessaire)
        var sort = Sort.by("dateEntreeStock").descending();
        var all = voitureRepo.findAll(sort);

        var filtered = all.stream()
                .filter(v -> v.getStatut() == StatutVoiture.EN_STOCK
                          || v.getStatut() == StatutVoiture.RESERVEE)
                .filter(v -> marque == null || v.getModele().getMarque().getNom().equalsIgnoreCase(marque))
                .filter(v -> prixMax == null || (v.getPrixVente() != null && v.getPrixVente().compareTo(prixMax) <= 0))
                .filter(v -> anneeMin == null || (v.getAnnee() != null && v.getAnnee() >= anneeMin))
                .map(v -> {
                    // Priorité : photo principale, sinon la première photo disponible
                    Long photoId = documentRepo
                            .findByVoiture_Id(v.getId()).stream()
                            .filter(d -> d.getType() == TypeDocument.PHOTO)
                            .sorted((a, b) -> Boolean.compare(b.isPrincipale(), a.isPrincipale()))
                            .map(Document::getId)
                            .findFirst().orElse(null);
                    return VoiturePublicDto.from(v, photoId);
                })
                .toList();

        var pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        Page<VoiturePublicDto> result = new PageImpl<>(
                start < filtered.size() ? filtered.subList(start, end) : List.of(),
                pageable, filtered.size());

        return ResponseEntity.ok(result);
    }

    /** Détail d'un véhicule avec IDs de toutes ses photos. */
    @GetMapping("/vehicules/{id}")
    public ResponseEntity<VoiturePublicDetailDto> detail(@PathVariable Long id) {
        var voiture = voitureRepo.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Véhicule introuvable"));

        // Principale en premier, puis les autres photos
        List<Long> photoIds = documentRepo.findByVoiture_Id(id).stream()
                .filter(d -> d.getType() == TypeDocument.PHOTO)
                .sorted((a, b) -> Boolean.compare(b.isPrincipale(), a.isPrincipale()))
                .map(Document::getId)
                .toList();

        return ResponseEntity.ok(VoiturePublicDetailDto.from(voiture, photoIds));
    }

    /** Serve une photo (binaire) depuis S3 / disque. */
    @GetMapping("/vehicules/photo/{documentId}")
    public ResponseEntity<Resource> photo(@PathVariable Long documentId) {
        Resource resource = documentService.loadAsResource(documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    /** Liste des marques disponibles pour le filtre catalogue. */
    @GetMapping("/marques")
    public ResponseEntity<List<String>> marques() {
        List<String> noms = marqueRepo.findAll(Sort.by("nom")).stream()
                .map(m -> m.getNom())
                .distinct()
                .toList();
        return ResponseEntity.ok(noms);
    }
}
