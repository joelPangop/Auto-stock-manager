package org.autostock.services;

import jakarta.transaction.Transactional;
import org.autostock.models.Paiement;
import org.autostock.repositories.PaiementRepository;
import org.autostock.repositories.VenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardServiceImp extends AbstractBaseService<Paiement, Long, PaiementRepository> implements DashboardService {

    @Autowired
    private VenteRepository venteRepository;

    @Transactional
    public void recomputeVenteTotals(Long venteId) {

        BigDecimal totalPaye = repository.sumMontantByVenteId(venteId);

//        venteRepository.updatePrixFinal(
//                venteId,
//                totalPaye != null ? totalPaye : BigDecimal.ZERO
//        );
    }

    @Override
    public Paiement create(Paiement entity) throws AccessDeniedException {
        return null;
    }

    @Override
    public Optional<Paiement> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public List<Paiement> findAll() {
        return List.of();
    }

    @Override
    public void deleteById(Long aLong) {

    }
}
