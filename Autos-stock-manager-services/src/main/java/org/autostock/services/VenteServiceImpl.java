package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.enums.StatutVoiture;
import org.autostock.enums.TypeMouvement;
import org.autostock.models.Client;
import org.autostock.models.User;
import org.autostock.models.Vente;
import org.autostock.models.Voiture;
import org.autostock.repositories.ClientRepository;
import org.autostock.repositories.UserRepository;
import org.autostock.repositories.VenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VenteServiceImpl extends AbstractBaseService<Vente, Long, VenteRepository> implements VenteService {

    @Autowired
    private VoitureService voitureService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockMouvementService stockMouvementService;

    @Override
    public Vente creerVente(Long idVoiture, Long idClient, Long idVendeur,
                            BigDecimal prixFinal, String modePaiement) throws AccessDeniedException {

        Voiture voiture = voitureService.findById(idVoiture)
                .orElseThrow(() -> new EntityNotFoundException("Voiture introuvable"));

        Client client = clientRepository.findById(idClient)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable"));

        User vendeur = userRepository.findById(idVendeur)
                .orElseThrow(() -> new EntityNotFoundException("Vendeur introuvable"));

        Vente vente = new Vente();
        vente.setVoiture(voiture);
        vente.setClient(client);
        vente.setVendeur(vendeur);
        vente.setDateVente(LocalDateTime.now());
        vente.setPrixFinal(prixFinal);
        vente.setModePaiement(modePaiement);

        Vente saved = repository.save(vente);

        voitureService.changerStatut(voiture.getId(), StatutVoiture.VENDUE);
        stockMouvementService.enregistrerMouvement(voiture, TypeMouvement.VENTE,
                "Vente #" + saved.getId() + " au client " + client.getNom());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vente> ventesDuClient(Long idClient) {
        return repository.findByClient_Id(idClient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vente> ventesDuVendeur(Long idVendeur) {
        return repository.findByVendeur_Id(idVendeur);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vente> findById(Long idVente) {
        return repository.findById(idVente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vente> ventesEntre(LocalDateTime debut, LocalDateTime fin) {
        return repository.findByDateVenteBetween(debut, fin);
    }
}
