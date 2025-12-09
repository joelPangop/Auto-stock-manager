package org.autostock.services;

import org.autostock.dtos.DocumentCreateDto;
import org.autostock.dtos.DocumentDto;
import org.autostock.dtos.DocumentUpdateDto;
import org.autostock.models.Document;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService extends IService<Document, Long> {

    DocumentDto ajouterDocument(Long idVoiture, MultipartFile file, DocumentCreateDto meta);

    List<Document> documentsVoiture(Long idVoiture);

    DocumentDto updateMeta(Long id, DocumentUpdateDto dto);

    Resource loadAsResource(Long id);

    void delete(Long id);
}
