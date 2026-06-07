package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.autostock.dtos.DocumentCreateDto;
import org.autostock.dtos.DocumentDto;
import org.autostock.dtos.DocumentUpdateDto;
import org.autostock.enums.TypeDocument;
import org.autostock.mappers.DocumentMapper;
import org.autostock.mappers.VoitureMapper;
import org.autostock.models.Document;
import org.autostock.models.DocumentPaiement;
import org.autostock.models.Paiement;
import org.autostock.models.Voiture;
import org.autostock.repositories.DepenseRepository;
import org.autostock.repositories.DocumentPaiementRepository;
import org.autostock.repositories.DocumentRepository;
import org.autostock.repositories.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class DocumentServiceImpl extends AbstractBaseService<Document, Long, DocumentRepository> implements DocumentService {

    @Autowired
    private VoitureRepository voitureRepository;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private VoitureMapper voitureMapper;

    @Autowired
    private DocumentPaiementRepository documentPaiementRepository;

    @Autowired
    private DepenseRepository depenseRepository;

    @Autowired(required = false)
    private S3StorageService s3Storage;

    private final Path root;

    public DocumentServiceImpl(
            @Value("${autostock.docs.dir:uploads}") String rootDir) {
        this.root = Path.of(rootDir).toAbsolutePath().normalize();
        try { Files.createDirectories(this.root); } catch (Exception ignored) {}
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> listByVoiture(Long voitureId) {
        return repository.findByVoiture_Id(voitureId).stream().map(documentMapper::toDto).toList();
    }

    @Override
    public DocumentDto ajouterDocument(Long idVoiture, MultipartFile file, DocumentCreateDto meta) {
        Voiture voiture = voitureRepository.findById(idVoiture)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));

        Document doc = new Document();
        doc.setVoiture(voiture);
        doc.setType(TypeDocument.valueOf(meta.getType()));
        doc.setDescription(meta.getDescription());
        doc.setDateUpload(LocalDateTime.now());

        if (s3Storage != null) {
            // Stockage S3 / LocalStack
            String key = s3Storage.upload(file, "voitures/" + idVoiture);
            doc.setNomFichier(S3StorageService.keyToFilename(key));
            doc.setUrlFichier(key); // clé S3, l'URL pré-signée est générée à la demande
            log.info("[Document] Fichier uploadé sur S3 : {}", key);
        } else {
            // Fallback : stockage disque local
            String safeName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path dest = root.resolve(safeName).normalize();
            try (var in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Erreur écriture fichier", e);
            }
            doc.setNomFichier(safeName);
            doc.setUrlFichier("/api/documents/file/" + safeName);
        }

        return documentMapper.toDto(repository.save(doc));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> documentsVoiture(Long idVoiture) {
        return repository.findByVoiture_Id(idVoiture);
    }

    @Transactional
    public DocumentDto updateMeta(Long id, DocumentUpdateDto patch) {
        var doc = repository.findById(id).orElseThrow();
        if (patch.type() != null) doc.setType(TypeDocument.valueOf(patch.type()));
        if (patch.description() != null) doc.setDescription(patch.description());
        return documentMapper.toDto(doc);
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(Long id) {
        var doc = repository.findById(id).orElseThrow();
        if (s3Storage != null) {
            byte[] data = s3Storage.download(doc.getUrlFichier());
            return new ByteArrayResource(data);
        }
        Path path = root.resolve(doc.getNomFichier()).normalize();
        try {
            Resource res = new UrlResource(path.toUri());
            if (!res.exists() || !res.isReadable()) {
                throw new RuntimeException("Fichier introuvable/illisible");
            }
            return res;
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL invalide", e);
        }
    }

    /**
     * Retourne le nom de fichier original d'un document (utile quand la Resource ne le porte pas).
     */
    @Transactional(readOnly = true)
    public String getDocumentFilename(Long id) {
        return repository.findById(id)
                .map(Document::getNomFichier)
                .orElse("document");
    }

    /**
     * Retourne une URL pré-signée S3 (ou l'URL locale) pour accéder au document.
     */
    @Transactional(readOnly = true)
    public String getAccessUrl(Long id) {
        var doc = repository.findById(id).orElseThrow();
        if (s3Storage != null) {
            return s3Storage.generatePresignedUrl(doc.getUrlFichier());
        }
        return doc.getUrlFichier();
    }

    @Transactional
    public void delete(Long id) {
        var doc = repository.findById(id).orElseThrow();

        // 1. Dissocier les dépenses liées : mettre document_id à NULL
        //    La dépense appartient à la voiture, pas à la photo/document
        var depenses = depenseRepository.findAllByDocument_Id(id);
        if (!depenses.isEmpty()) {
            depenses.forEach(d -> d.setDocument(null));
            depenseRepository.saveAll(depenses);
            log.info("[Document] {} dépense(s) dissociée(s) du document {}", depenses.size(), id);
        }

        // 2. Supprimer le fichier physique (S3 ou disque)
        if (s3Storage != null && doc.getUrlFichier() != null && !doc.getUrlFichier().isBlank()) {
            try { s3Storage.delete(doc.getUrlFichier()); } catch (Exception e) {
                log.warn("[Document] Impossible de supprimer le fichier S3 {} : {}", doc.getUrlFichier(), e.getMessage());
            }
        } else {
            try { Files.deleteIfExists(root.resolve(doc.getNomFichier())); } catch (IOException ignored) {}
        }

        // 3. Supprimer le document
        repository.delete(doc);
        log.info("[Document] Document {} supprimé avec succès", id);
    }

    @Transactional
    public void saveReceiptForPaiement(Paiement paiement, byte[] pdf) {
        DocumentPaiement docPaiement = new DocumentPaiement();
        Document doc = new Document();
        doc.setMontant(paiement.getMontant());
        doc.setType(TypeDocument.RECU_PAIEMENT);
        String filename = UUID.randomUUID() + "_recu_paiement_" + LocalDateTime.now() + ".pdf";
        doc.setNomFichier(filename);
        doc.setDateUpload(LocalDateTime.now());

        if (s3Storage != null) {
            String key = "recus/" + filename;
            s3Storage.uploadBytes(pdf, key, "application/pdf");
            doc.setUrlFichier(key);
        }

        docPaiement.setDocument(doc);
        docPaiement.setPaiement(paiement);
        docPaiement.setContent(pdf);
        docPaiement.setMimeType("application/pdf");

        documentPaiementRepository.save(docPaiement);
    }


}
