package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.models.Document;
import org.autostock.models.Voiture;
import org.autostock.repositories.DocumentRepository;
import org.autostock.repositories.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DocumentServiceImpl extends AbstractBaseService<Document, Long, DocumentRepository> implements DocumentService {

    @Autowired
    private VoitureRepository voitureRepository;

    @Override
    public Document ajouterDocument(Long idVoiture, Document document) {
        Voiture voiture = voitureRepository.findById(idVoiture)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));

        document.setVoiture(voiture);
        if (document.getDateUpload() == null) {
            document.setDateUpload(LocalDateTime.now());
        }
        return repository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Document> documentsVoiture(Long idVoiture) {
        return repository.findByVoiture_Id(idVoiture);
    }
}
