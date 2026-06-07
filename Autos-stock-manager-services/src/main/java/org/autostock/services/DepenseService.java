package org.autostock.services;

import org.autostock.dtos.*;
import org.autostock.models.Depense;

import java.time.LocalDateTime;
import java.util.List;

public interface DepenseService extends IService<Depense, Long> {
    DepenseDto create(Long voitureId, DepenseCreateDto dto);
    PageVm<DepenseDto> listByVoiture(Long voitureId, int page, int size);
    DepenseDto getById(Long voitureId, Long depenseId);
    void deleteByEntretienId(Long entretienId);
//    DepenseDto getByEntretienId(Long entretienId);
    PageVm<DepenseDto> listNonJustifiees(Long voitureId, int page, int size);
    List<DepenseMonthlyTotalDto> monthlyTotals(Long voitureId, LocalDateTime start, LocalDateTime end);
    List<DepenseMonthlyByCategorieDto> monthlyTotalsByCategorie(Long voitureId, LocalDateTime start, LocalDateTime end);
    DepenseDashboardVm dashboard(Long voitureId,
                                 Integer page, Integer size,
                                 LocalDateTime start, LocalDateTime end,
                                 boolean includeMonthly,
                                 boolean includeMonthlyByCategorie);
    void delete(Long voitureId, Long depenseId);
    DepenseDto update(Long voitureId, Long depenseId, DepenseCreateDto dto);
}
