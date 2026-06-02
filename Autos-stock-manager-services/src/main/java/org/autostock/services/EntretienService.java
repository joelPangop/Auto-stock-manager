package org.autostock.services;

import org.autostock.dtos.EntretienDto;
import org.autostock.dtos.PageVm;
import org.autostock.models.Entretien;

import java.util.List;

public interface EntretienService extends IService<Entretien, Long> {

    Entretien ajouterEntretien(Long idVoiture, Entretien entretien);

    List<Entretien> entretiensVoiture(Long idVoiture);

    Entretien modifierEntretien(Long depenseId, Entretien entretien);
    PageVm<EntretienDto> getPage(int page, int size, String sort, boolean onlyMine);
}
