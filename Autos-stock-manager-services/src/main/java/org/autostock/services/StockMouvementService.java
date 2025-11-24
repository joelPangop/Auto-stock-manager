package org.autostock.services;

import org.autostock.enums.TypeMouvement;
import org.autostock.models.StockMouvement;
import org.autostock.models.Voiture;

import java.util.List;

public interface StockMouvementService extends IService<StockMouvement, Long> {

    StockMouvement enregistrerMouvement(Voiture voiture, TypeMouvement type, String commentaire);

    List<StockMouvement> historiqueVoiture(Long idVoiture);
}
