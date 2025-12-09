package org.autostock.services;

import org.autostock.dtos.VoitureUpdateDto;
import org.autostock.enums.StatutVoiture;
import org.autostock.models.Voiture;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

public interface VoitureService extends IService<Voiture, Long> {

    List<Voiture> listerVoituresEnStock();

    List<Voiture> listerVoituresParMarqueEtStatut(String nomMarque, StatutVoiture statut);

    Optional<Voiture> trouverParVin(String vin);

    List<Voiture> listMine() throws AccessDeniedException;

    Voiture changerStatut(Long idVoiture, StatutVoiture statutVoiture) throws AccessDeniedException;

    Voiture update(Long id, Voiture voiture) throws AccessDeniedException;
}
