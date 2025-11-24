package org.autostock.services;

import org.autostock.models.Client;

import java.util.List;
import java.util.Optional;

public interface ClientService extends IService<Client, Long> {

    Optional<Client> trouverParEmail(String email);

    List<Client> rechercherParNom(String nomPartiel);
}
