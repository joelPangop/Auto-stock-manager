package org.autostock.services;

import org.autostock.models.Client;

import java.util.List;
import java.util.Optional;

public interface ClientService extends IService<Client, Long> {

    Optional<Client> trouverParEmail(String email);

    /** Met a jour les champs editables d'un client existant. */
    Client modifier(Long id, Client patch);

    List<Client> rechercherParNom(String nomPartiel);
}
