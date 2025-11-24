package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.enums.MethodePaiement;
import org.autostock.models.Paiement;
import org.autostock.models.Vente;
import org.autostock.repositories.PaiementRepository;
import org.autostock.repositories.VenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaiementServiceImpl extends AbstractBaseService<Paiement, Long, PaiementRepository> implements PaiementService {

    @Autowired
    private VenteRepository venteRepository;

    @Override
    public Paiement ajouterPaiement(Long idVente, BigDecimal montant, String methodeValue) {
        Vente vente = venteRepository.findById(idVente)
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable"));

        MethodePaiement methode = MethodePaiement.fromValue(methodeValue);

        Paiement p = new Paiement();
        p.setVente(vente);
        p.setMontant(montant);
        p.setMethode(methode);
        p.setDatePaiement(LocalDateTime.now());

        return repository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Paiement> paiementsDeLaVente(Long idVente) {
        return repository.findByVente_Id(idVente);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal totalPaye(Long idVente) {
        return repository.findByVente_Id(idVente).stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal resteAPayer(Long idVente) {
        Vente vente = venteRepository.findById(idVente)
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable"));
        return vente.getPrixFinal().subtract(totalPaye(idVente));
    }
}
