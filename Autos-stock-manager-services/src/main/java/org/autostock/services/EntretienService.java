package org.autostock.services;

import org.autostock.models.Entretien;

import java.util.List;

public interface EntretienService extends IService<Entretien, Long> {

    Entretien ajouterEntretien(Long idVoiture, Entretien entretien);

    List<Entretien> entretiensVoiture(Long idVoiture);
}
