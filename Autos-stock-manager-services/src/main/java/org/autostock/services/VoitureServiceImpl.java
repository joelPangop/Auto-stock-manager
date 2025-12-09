package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.configs.SecurityUtils;
import org.autostock.enums.StatutVoiture;
import org.autostock.enums.TypeMouvement;
import org.autostock.models.User;
import org.autostock.models.Voiture;
import org.autostock.repositories.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VoitureServiceImpl extends AbstractBaseService<Voiture, Long, VoitureRepository> implements VoitureService {

    @Autowired
    private StockMouvementService stockMouvementService;

    @Autowired
    private SecurityUtils sec;

    @Override
    public Voiture create(Voiture voiture) throws AccessDeniedException {
        voiture.setStatut(StatutVoiture.EN_STOCK);
        voiture.setDateEntreeStock(LocalDateTime.now());
        voiture.setOwner(new User(sec.currentUserId()));
        Voiture saved = repository.save(voiture);
        stockMouvementService.enregistrerMouvement(saved, TypeMouvement.ENTREE, "Ajout initial au stock");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Voiture> listerVoituresEnStock() {
        return repository.findByStatut(StatutVoiture.EN_STOCK);
    }

    @Override
    public List<Voiture> listerVoituresParMarqueEtStatut(String nomMarque, StatutVoiture statut) {

        return List.of();
    }

    @Transactional(readOnly = true)
    public List<Voiture> listMine() throws AccessDeniedException {
        return repository.findByOwner_Id(sec.currentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voiture> trouverParVin(String vin) {
        return repository.findByVin(vin);
    }

    @Override
    public Voiture changerStatut(Long idVoiture, StatutVoiture statutVoiture) throws AccessDeniedException {
        Voiture v = repository.findById(idVoiture)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));
        if (!sec.isAdmin() && !v.getOwner().getId().equals(sec.currentUserId())) {
            throw new AccessDeniedException("Vous n’êtes pas propriétaire");
        }
        v.setStatut(statutVoiture);
        Voiture saved = repository.save(v);
        switch (statutVoiture) {
            case VENDUE -> stockMouvementService.enregistrerMouvement(saved, TypeMouvement.VENTE, "Vente du véhicule");
            case RESERVEE ->
                    stockMouvementService.enregistrerMouvement(saved, TypeMouvement.RESERVATION, "Réservation du véhicule");
            case EN_STOCK ->
                    stockMouvementService.enregistrerMouvement(saved, TypeMouvement.RETOUR, "Annulation de la réservation");
        }

        return saved;
    }

    @Transactional
    public Voiture update(Long id, Voiture v) throws AccessDeniedException {
        Voiture existedVoiture = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));
        if (!sec.isAdmin() && !v.getOwner().getId().equals(sec.currentUserId())) {
            throw new AccessDeniedException("Vous n’êtes pas propriétaire");
        }
        v.setId(existedVoiture.getId());
        v.setVente(existedVoiture.getVente());
        v.setDateEntreeStock(existedVoiture.getDateEntreeStock());
        v.setVersion(existedVoiture.getVersion());
        return repository.save(v);
    }

}
