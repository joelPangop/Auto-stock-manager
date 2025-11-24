package org.autostock.services;

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
