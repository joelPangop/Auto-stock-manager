package org.autostock.services;

import org.autostock.dtos.MarqueCreateDto;
import org.autostock.dtos.MarqueDto;
import org.autostock.models.Marque;
import org.autostock.repositories.MarqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@Transactional
public class MarqueServiceImp extends AbstractBaseService<Marque, Long, MarqueRepository> implements MarqueService {

    @Override
    public List<MarqueDto> list() {
        return findAll().stream().map(m -> new MarqueDto(m.getId(), m.getNom())).toList();
    }

    @Override
    @Transactional
    public MarqueDto create(MarqueCreateDto dto) throws AccessDeniedException {
        Marque m = new Marque();
        m.setNom(dto.nom());
        m = create(m);
        return new MarqueDto(m.getId(), m.getNom());
    }

}
