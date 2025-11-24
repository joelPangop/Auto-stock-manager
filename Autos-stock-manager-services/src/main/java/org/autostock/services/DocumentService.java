package org.autostock.services;

import org.autostock.models.Document;

import java.util.List;

public interface DocumentService extends IService<Document, Long> {

    Document ajouterDocument(Long idVoiture, Document document);

    List<Document> documentsVoiture(Long idVoiture);
}
