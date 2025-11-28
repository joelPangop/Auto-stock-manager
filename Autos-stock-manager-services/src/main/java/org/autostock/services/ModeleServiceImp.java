package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.dtos.ModeleCreateDto;
import org.autostock.dtos.ModeleDto;
import org.autostock.models.Modele;
import org.autostock.repositories.MarqueRepository;
import org.autostock.repositories.ModeleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ModeleServiceImp extends AbstractBaseService<Modele, Long, ModeleRepository> implements ModeleService {

    @Autowired
    private MarqueRepository marqueRepository;

    @Override
    public List<ModeleDto> listByMarque(Long idMarque) {
        return repository.findByMarque_Id(idMarque)
                .stream().map(md -> new ModeleDto(md.getId(), md.getNom(), md.getMarque().getId())).toList();
    }

    @Override
    @Transactional
    public ModeleDto create(ModeleCreateDto dto) {
        var marque = marqueRepository.findById(dto.idMarque())
                .orElseThrow(() ->  new EntityNotFoundException("Marque " + dto.idMarque()));
        Modele md = new Modele(); md.setNom(dto.nom()); md.setMarque(marque);
        md = create(md);
        return new ModeleDto(md.getId(), md.getNom(), marque.getId());
    }
}
