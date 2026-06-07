package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.DepenseCreateDto;
import org.autostock.dtos.DocumentCreateDto;
import org.autostock.dtos.DocumentDto;
import org.autostock.dtos.DocumentUpdateDto;
import org.autostock.enums.CategorieDepense;
import org.autostock.mappers.DocumentMapper;
import org.autostock.models.Voiture;
import org.autostock.services.DepenseService;
import org.autostock.services.DocumentService;
import org.autostock.services.VoitureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private final DocumentMapper documentMapper;

    @Autowired
    private VoitureService voitureService;

    @Autowired
    private DepenseService depenseService;

    @PostMapping(value = "/voiture/{voitureId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentDto upload(@PathVariable Long voitureId,
                              @RequestPart("file") MultipartFile file,
                              @RequestPart("meta") DocumentCreateDto meta) {

        Optional<Voiture> voiture = voitureService.findById(voitureId);
        DocumentDto savedDoc = documentService.ajouterDocument(voitureId, file, meta);
//        if (meta.getDepense() != null && meta.getDepense().getMontant() != null) {

        DepenseCreateDto dDto = new DepenseCreateDto();
        dDto.setVoitureId(voiture.get().getId());
        dDto.setCategorie(CategorieDepense.FRAIS_ADMIN);
        dDto.setDescription(meta.getDescription() == null || meta.getDescription().isEmpty() ? savedDoc.getTypeLabel() : meta.getDescription());
        dDto.setMontant(meta.getMontant() == null ? BigDecimal.valueOf(0) : meta.getMontant());
        dDto.setDocumentId(savedDoc.getId());
        dDto.setDateDepense(savedDoc.getDateUpload());

        depenseService.create(voitureId, dDto);

        return savedDoc;
}

@GetMapping("/{idVoiture}")
public List<DocumentDto> list(@PathVariable Long idVoiture) {
    return documentService.documentsVoiture(idVoiture).stream()
            .map(documentMapper::toDto).toList();
}

// Update métadonnées (pas le binaire)
@PatchMapping("/{id}")
public DocumentDto updateMeta(@PathVariable Long id, @RequestBody DocumentUpdateDto patch) {
    return documentService.updateMeta(id, patch);
}

// Download (stream)
@GetMapping("/{id}/download")
public ResponseEntity<Resource> download(@PathVariable Long id) {

    Resource resource = documentService.loadAsResource(id);

    // getFilename() peut être null (ByteArrayResource depuis S3) — on récupère le nom depuis la BDD
    String rawFilename = resource.getFilename();
    String filename = (rawFilename != null)
            ? Paths.get(rawFilename).getFileName().toString()
            : documentService.getDocumentFilename(id);

    MediaType contentType = MediaTypeFactory.getMediaType(filename)
            .orElse(MediaType.APPLICATION_OCTET_STREAM);

    ContentDisposition cd = ContentDisposition.attachment()
            .filename(filename, StandardCharsets.UTF_8)
            .build();

    return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
            .cacheControl(CacheControl.noCache())
            .body(resource);
}

// Delete
@DeleteMapping("/{id}")
public void delete(@PathVariable Long id) {
    documentService.delete(id);
}
}
