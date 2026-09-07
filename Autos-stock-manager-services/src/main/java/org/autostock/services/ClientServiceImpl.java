package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.models.Client;
import org.autostock.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClientServiceImpl extends AbstractBaseService<Client, Long, ClientRepository> implements ClientService {

    @Override
    public Client modifier(Long id, Client patch) {
        Client existant = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable"));
        // Seuls les champs du formulaire sont repris : reprendre l'entite du
        // corps de la requete effacerait l'historique des ventes et la version.
        existant.setNom(patch.getNom());
        existant.setEmail(patch.getEmail());
        existant.setTelephone(patch.getTelephone());
        existant.setAdresse(patch.getAdresse());
        return repository.save(existant);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> trouverParEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> rechercherParNom(String nomPartiel) {
        return repository.findByNomContainingIgnoreCase(nomPartiel);
    }
}
