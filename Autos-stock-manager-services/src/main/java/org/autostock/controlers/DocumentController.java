package org.autostock.controlers;

import lombok.RequiredArgsConstructor;
import org.autostock.dtos.DocumentCreateDto;
import org.autostock.dtos.DocumentDto;
import org.autostock.mappers.DocumentMapper;
import org.autostock.models.Document;
import org.autostock.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voitures/{idVoiture}/documents")
@RequiredArgsConstructor
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private final DocumentMapper documentMapper;

    @PostMapping
    public DocumentDto add(@PathVariable Long idVoiture, @RequestBody DocumentCreateDto dto) {
        Document d = documentService.ajouterDocument(idVoiture, documentMapper.toEntity(dto));
        return documentMapper.toDto(d);
    }

    @GetMapping
    public List<DocumentDto> list(@PathVariable Long idVoiture) {
        return documentService.documentsVoiture(idVoiture).stream()
                .map(documentMapper::toDto).toList();
    }
}
