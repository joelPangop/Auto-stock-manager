package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.dtos.EntretienDto;
import org.autostock.dtos.PageVm;
import org.autostock.mappers.EntretienMapper;
import org.autostock.models.Entretien;
import org.autostock.models.Voiture;
import org.autostock.repositories.EntretienRepository;
import org.autostock.repositories.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EntretienServiceImpl extends AbstractBaseService<Entretien, Long, EntretienRepository> implements EntretienService {

    @Autowired
    private VoitureRepository voitureRepository;

    @Autowired
    private EntretienMapper mapper;

    @Override
    public Entretien ajouterEntretien(Long idVoiture, Entretien entretien) {
        Voiture voiture = voitureRepository.findById(idVoiture)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));

        entretien.setVoiture(voiture);
        if (entretien.getDateEntretien() == null) {
            entretien.setDateEntretien(LocalDateTime.now());
        }
        return repository.save(entretien);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Entretien> entretiensVoiture(Long idVoiture) {
        return repository.findByVoiture_Id(idVoiture);
    }

    @Override
    public Entretien modifierEntretien(Long entretienId, Entretien entretien) {
        Entretien entretien1 = repository.findById(entretienId).orElseThrow();
        if (entretien.getVoiture() != null) {
            entretien1.setVoiture(entretien.getVoiture());
        }
        entretien1.setDateEntretien(entretien.getDateEntretien());
        entretien1.setType(entretien.getType());
        entretien1.setGarage(entretien.getGarage());
        entretien1.setCout(entretien.getCout());
        entretien1.setDescription(entretien.getDescription());

        return repository.save(entretien1);
    }

    @Override
    @Transactional(readOnly = true)
    public PageVm<EntretienDto> getPage(int page, int size, String sort, boolean onlyMine) {

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));

        Page<Entretien> p = repository.findAll(pageable);;

//        if (onlyMine) {
////            Long userId = currentUserService.requireUserId();
////            // ⚠️ adapte "ownerId" si ton champ s'appelle autrement
////            p = repository.findByOwnerId(userId, pageable);
//        } else {
//            p = repository.findAll(pageable);
//        }

        List<EntretienDto> items = p.getContent().stream()
                .map(mapper::toDto)
                .toList();

        return new PageVm<>(
                items,
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );
    }

    private Sort parseSort(String sort) {
        // attend "dateEntretien,desc" ou "dateEntretien,asc"
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "dateEntretien");
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(dir, field);
    }
}

