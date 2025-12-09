package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.dtos.DocumentCreateDto;
import org.autostock.dtos.DocumentDto;
import org.autostock.dtos.DocumentUpdateDto;
import org.autostock.enums.TypeDocument;
import org.autostock.mappers.DocumentMapper;
import org.autostock.mappers.VoitureMapper;
import org.autostock.models.Document;
import org.autostock.models.Voiture;
import org.autostock.repositories.DocumentRepository;
import org.autostock.repositories.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

@Service
@Transactional
public class DocumentServiceImpl extends AbstractBaseService<Document, Long, DocumentRepository> implements DocumentService {

    @Autowired
    private VoitureRepository voitureRepository;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private VoitureMapper voitureMapper;

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

        String safeName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path dest = root.resolve(safeName).normalize();

        try (var in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Erreur écriture fichier", e);
        }

        Document doc = new Document();
        doc.setVoiture(voiture);
        doc.setType(TypeDocument.valueOf(meta.getType()));
        doc.setDescription(meta.getDescription());
        doc.setNomFichier(safeName);
        doc.setUrlFichier("/api/documents/" + "file/" + safeName); // optionnel
        doc.setDateUpload(LocalDateTime.now());

        Document document = repository.save(doc);
        return documentMapper.toDto(document);
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

    @Transactional
    public void delete(Long id) {
        var doc = repository.findById(id).orElseThrow();
        try { Files.deleteIfExists(root.resolve(doc.getNomFichier())); } catch (IOException ignored) {}
        repository.delete(doc);
    }

}
