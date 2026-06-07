package org.autostock.services;

import org.autostock.dtos.*;
import org.autostock.mappers.DepenseMapper;
import org.autostock.models.*;
import org.autostock.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepenseServiceImp extends AbstractBaseService<Depense, Long, DepenseRepository> implements DepenseService {

    @Autowired
    VoitureRepository voitureRepository;

    @Autowired
    EntretienRepository entretienRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    FournisseurRepository fournisseurRepository;

    @Autowired
    DepenseMapper depenseMapper;

    @Override
    public DepenseDto create(Long voitureId, DepenseCreateDto dto) {
        // sécurité: on force la voitureId du path
        dto.setVoitureId(voitureId);

        Voiture voiture = voitureRepository.findById(voitureId)
                .orElseThrow(() -> new IllegalArgumentException("Voiture introuvable: " + voitureId));

        Entretien entretien = dto.getEntretienId() == null ? null :
                entretienRepository.findById(dto.getEntretienId())
                        .orElseThrow(() -> new IllegalArgumentException("Entretien introuvable: " + dto.getEntretienId()));

        Document document = dto.getDocumentId() == null ? null :
                documentRepository.findById(dto.getDocumentId())
                        .orElseThrow(() -> new IllegalArgumentException("Document introuvable: " + dto.getDocumentId()));

        Fournisseur fournisseur = dto.getFournisseurId() == null ? null :
                fournisseurRepository.findById(dto.getFournisseurId())
                        .orElseThrow(() -> new IllegalArgumentException("Fournisseur introuvable: " + dto.getFournisseurId()));

        // (Optionnel) règle: au moins un justificatif
        // if (entretien == null && document == null) {
        //     throw new IllegalArgumentException("Une dépense doit être liée à un entretien ou un document.");
        // }

        Depense depense = depenseMapper.toEntity(dto, voiture, entretien, document, fournisseur);
        Depense saved = repository.save(depense);
        return depenseMapper.toDto(saved);
    }

    public PageVm<DepenseDto> listByVoiture(Long voitureId, int page, int size) {
        Page<Depense> p = repository.findByVoiture_Id(
                voitureId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateDepense"))
        );

        List<DepenseDto> items = p.getContent().stream().map(depenseMapper::toDto).toList();
        return new PageVm<>(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    public DepenseDto getById(Long voitureId, Long depenseId) {
        Depense depense = repository.findByVoiture_IdAndId(voitureId, depenseId);
        return depenseMapper.toDto(depense);
    }

    public void deleteByEntretienId(Long entretienId) {
       repository.deleteByEntretienId(entretienId);
    }

    public PageVm<DepenseDto> listNonJustifiees(Long voitureId, int page, int size) {
        Page<Depense> p = repository.findByVoiture_IdAndDocumentIsNullAndEntretienIsNull(
                voitureId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateDepense"))
        );

        List<DepenseDto> items = p.getContent().stream().map(depenseMapper::toDto).toList();
        return new PageVm<>(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    public List<DepenseMonthlyTotalDto> monthlyTotals(Long voitureId, LocalDateTime start, LocalDateTime end) {
        List<MensuelTotalProjection> rows = repository.monthlyTotals(voitureId, start, end);
        return rows.stream()
                .map(r -> new DepenseMonthlyTotalDto(r.getYear(), r.getMonth(), r.getTotal()))
                .toList();
    }

    public List<DepenseMonthlyByCategorieDto> monthlyTotalsByCategorie(Long voitureId, LocalDateTime start, LocalDateTime end) {
        List<MensuelCategorieTotalProjection> rows = repository.monthlyTotalsByCategorie(voitureId, start, end);
        return rows.stream()
                .map(r -> new DepenseMonthlyByCategorieDto(r.getYear(), r.getMonth(), r.getCategorie(), r.getTotal()))
                .toList();
    }

    public DepenseDashboardVm dashboard(Long voitureId,
                                        Integer page, Integer size,
                                        LocalDateTime start, LocalDateTime end,
                                        boolean includeMonthly,
                                        boolean includeMonthlyByCategorie) {

        int p = page != null ? page : 0;
        int s = size != null ? size : 10;

        // liste
        PageVm<DepenseDto> pageVm = listByVoiture(voitureId, p, s);

        // totaux
        BigDecimal total = repository.totalByVoiture(voitureId);
        BigDecimal totalPeriode = (start != null && end != null)
                ? repository.totalByVoitureAndPeriod(voitureId, start, end)
                : null;

        DepenseDashboardVm vm = new DepenseDashboardVm();
        vm.setTotal(total);
        vm.setTotalPeriode(totalPeriode);
        vm.setPage(pageVm);

        // graph
        if (includeMonthly && start != null && end != null) {
            vm.setMonthly(monthlyTotals(voitureId, start, end));
        }
        if (includeMonthlyByCategorie && start != null && end != null) {
            vm.setMonthlyByCategorie(monthlyTotalsByCategorie(voitureId, start, end));
        }

        return vm;
    }

    public void delete(Long voitureId, Long depenseId) {
        repository.deleteByVoiture_IdAndId(voitureId, depenseId);
    }

    public DepenseDto update(Long voitureId, Long depenseId, DepenseCreateDto dto) {
        Depense doc = repository.findById(depenseId).orElseThrow();
        doc.setDateDepense(dto.getDateDepense());
        doc.setDescription(dto.getDescription());
        doc.setMontant(dto.getMontant());
        doc.setDateDepense(dto.getDateDepense());
        doc.setCategorie(dto.getCategorie());
        Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId()).orElseThrow();
        if (fournisseur != null) {
            doc.setFournisseur(fournisseur);
        }

//        Depense depensToUpdade = depenseMapper.toEntity(doc);
        return depenseMapper.toDto(repository.save(doc));
    }
}
