package org.autostock.services;

import org.autostock.dtos.PageVm;
import org.autostock.dtos.PaiementDto;
import org.autostock.models.Paiement;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

public interface PaiementService extends IService<Paiement, Long> {

    Paiement ajouterPaiement(Long idVente, BigDecimal montant, String methodeValue);

    List<Paiement> paiementsDeLaVente(Long idVente);

    BigDecimal totalPaye(Long idVente);

    BigDecimal resteAPayer(Long idVente);
    PageVm<PaiementDto> getPage(int page, int size, String sort, boolean onlyMine) throws AccessDeniedException;
    PaiementDto getById(Long id);
    void deleteById(Long id);

    PaiementDto update(Long id, PaiementDto dto);
}
