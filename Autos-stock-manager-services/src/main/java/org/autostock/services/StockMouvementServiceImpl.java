package org.autostock.services;

import org.autostock.enums.TypeMouvement;
import org.autostock.models.StockMouvement;
import org.autostock.models.Voiture;
import org.autostock.repositories.StockMouvementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class StockMouvementServiceImpl extends AbstractBaseService<StockMouvement, Long, StockMouvementRepository>
        implements StockMouvementService {

    @Autowired
    public StockMouvementServiceImpl(StockMouvementRepository repository) {
        this.repository = repository;
    }

    @Override
    public StockMouvement enregistrerMouvement(Voiture voiture, TypeMouvement type, String commentaire) {
        StockMouvement m = new StockMouvement();
        m.setVoiture(voiture);
        m.setType(type);
        m.setDateMouvement(LocalDateTime.now());
        m.setCommentaire(commentaire);
        return repository.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMouvement> historiqueVoiture(Long idVoiture) {
        return repository.findByVoiture_Id(idVoiture);
    }
}
