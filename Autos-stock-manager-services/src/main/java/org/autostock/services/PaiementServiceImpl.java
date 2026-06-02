package org.autostock.services;

import jakarta.persistence.EntityNotFoundException;
import org.autostock.configs.SecurityUtils;
import org.autostock.dtos.PageVm;
import org.autostock.dtos.PaiementDto;
import org.autostock.enums.MethodePaiement;
import org.autostock.mappers.PaiementMapper;
import org.autostock.models.Paiement;
import org.autostock.models.User;
import org.autostock.models.Vente;
import org.autostock.repositories.PaiementRepository;
import org.autostock.repositories.UserRepository;
import org.autostock.repositories.VenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaiementServiceImpl extends AbstractBaseService<Paiement, Long, PaiementRepository> implements PaiementService {

    @Autowired
    private VenteRepository venteRepository;

    @Autowired
    private PaiementMapper mapper;

    @Autowired
    private SecurityUtils sec;

    @Autowired
    private UserRepository userRepository;
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

        Paiement saved = repository.save(p);

        return saved;
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

    @Override
    @Transactional(readOnly = true)
    public PageVm<PaiementDto> getPage(int page, int size, String sort, boolean onlyMine) throws AccessDeniedException {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Optional<User> user = Optional.of(userRepository.getReferenceById(sec.currentUserId()));
        Page<Paiement> p= repository.findAll(pageable);
//        if (onlyMine) {
//            Long userId = user.get().getId();
//            // ⚠️ Ici on suppose que Vente a un ownerId (ou createdById)
//            // et que Paiement est lié à Vente.
////            p = repository.findAllByUser(userId, pageable);
//        } else {
//            p = repository.findAll(pageable);
//        }
        List<PaiementDto> items = p.getContent()
                .stream()
                .map(mapper::toDto)
                .toList();

        return new PageVm<>(
                items,
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaiementDto getById(Long id) {
        Paiement p = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paiement introuvable"));
        return mapper.toDto(p);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public PaiementDto update(Long id, PaiementDto dto) {
        Paiement p = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paiement introuvable"));

        // champs éditables (à toi de décider)
        p.setMontant(dto.getMontant());
        p.setDatePaiement(dto.getDatePaiement());
//        p.setMethode(dto.getMethode());
//        p.setReference(dto.getReference());

        Paiement saved = repository.save(p);
        return mapper.toDto(saved);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "datePaiement");
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

}
