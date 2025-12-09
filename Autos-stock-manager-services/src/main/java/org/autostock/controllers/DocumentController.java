package org.autostock.controllers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.DocumentCreateDto;
import org.autostock.dtos.DocumentDto;
import org.autostock.dtos.DocumentUpdateDto;
import org.autostock.mappers.DocumentMapper;
import org.autostock.models.Document;
import org.autostock.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private final DocumentMapper documentMapper;

    @PostMapping(value = "/voiture/{voitureId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentDto upload(@PathVariable Long voitureId,
                              @RequestPart("file") MultipartFile file,
                              @RequestPart("meta") DocumentCreateDto meta) {
        return documentService.ajouterDocument(voitureId, file, meta);
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
        String filename = Paths.get(Objects.requireNonNull(resource.getFilename())).getFileName().toString();
        // Deviner le content-type
        MediaType contentType = MediaTypeFactory.getMediaType(filename)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        // Nom de fichier
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(filename.toString(), StandardCharsets.UTF_8)
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
