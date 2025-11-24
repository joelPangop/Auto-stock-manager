package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.models.Entretien;
import org.autostock.models.Voiture;
import org.autostock.repositories.EntretienRepository;
import org.autostock.repositories.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EntretienServiceImpl extends AbstractBaseService<Entretien, Long, EntretienRepository> implements EntretienService {

    @Autowired
    private VoitureRepository voitureRepository;

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
}

