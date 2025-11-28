package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.dtos.VoitureUpdateDto;
import org.autostock.enums.StatutVoiture;
import org.autostock.enums.TypeMouvement;
import org.autostock.models.Fournisseur;
import org.autostock.models.Modele;
import org.autostock.models.Voiture;
import org.autostock.repositories.FournisseurRepository;
import org.autostock.repositories.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VoitureServiceImpl extends AbstractBaseService<Voiture, Long, VoitureRepository> implements VoitureService {

    @Autowired
    private StockMouvementService stockMouvementService;

    @Autowired
    private FournisseurRepository fournisseurRepository;

    @Override
    public Voiture create(Voiture voiture) {
        voiture.setStatut(StatutVoiture.EN_STOCK);
        voiture.setDateEntreeStock(LocalDateTime.now());
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

    @Override
    @Transactional(readOnly = true)
    public Optional<Voiture> trouverParVin(String vin) {
        return repository.findByVin(vin);
    }

    @Override
    public Voiture reserverVoiture(Long idVoiture) {
        Voiture v = repository.findById(idVoiture)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));
        v.setStatut(StatutVoiture.RESERVEE);
        Voiture saved = repository.save(v);
        stockMouvementService.enregistrerMouvement(saved, TypeMouvement.RESERVATION, "Réservation du véhicule");
        return saved;
    }

    @Override
    public Voiture libererReservation(Long idVoiture) {
        Voiture v = repository.findById(idVoiture)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));
        v.setStatut(StatutVoiture.EN_STOCK);
        Voiture saved = repository.save(v);
        stockMouvementService.enregistrerMouvement(saved, TypeMouvement.RETOUR, "Annulation de la réservation");
        return saved;
    }

    @Override
    public Voiture marquerVendue(Long idVoiture) {
        Voiture v = repository.findById(idVoiture)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));
        v.setStatut(StatutVoiture.VENDUE);
        Voiture saved = repository.save(v);
        stockMouvementService.enregistrerMouvement(saved, TypeMouvement.VENTE, "Vente du véhicule");
        return saved;
    }

    @Transactional
    public Voiture update(Long id, Voiture v) {
        Voiture existedVoiture = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));;
                v.setId(existedVoiture.getId());
                v.setVente(existedVoiture.getVente());
                v.setDateEntreeStock(existedVoiture.getDateEntreeStock());
                v.setVersion(existedVoiture.getVersion());
        return repository.save(v);
    }

}
