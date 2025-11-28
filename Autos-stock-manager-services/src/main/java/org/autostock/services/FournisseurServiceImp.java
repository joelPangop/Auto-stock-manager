package org.autostock.services;

import org.autostock.dtos.FournisseurDto;
import org.autostock.mappers.FournisseurMapper;
import org.autostock.models.Fournisseur;
import lombok.RequiredArgsConstructor;
import org.autostock.repositories.FournisseurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FournisseurServiceImp extends AbstractBaseService<Fournisseur, Long, FournisseurRepository> implements FournisseurService  {

    @Autowired
    private FournisseurMapper fournisseurMapper;

    public List<FournisseurDto> getAllFournisseurs() {
        return findAll().stream()
                .map(fournisseurMapper::toDto)
                .collect(Collectors.toList());
    }

    public FournisseurDto getFournisseurById(Long id) {
        Fournisseur fournisseur = findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        return fournisseurMapper.toDto(fournisseur);
    }

    public FournisseurDto createFournisseur(FournisseurDto fournisseurDto) {
        Fournisseur fournisseur = fournisseurMapper.toEntity(fournisseurDto);
        Fournisseur savedFournisseur = create(fournisseur);
        return fournisseurMapper.toDto(savedFournisseur);
    }

    public FournisseurDto updateFournisseur(Long id, FournisseurDto fournisseurDto) {
        Fournisseur existingFournisseur = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));

        existingFournisseur.setNom(fournisseurDto.getNom());
        existingFournisseur.setType(fournisseurDto.getType());
        existingFournisseur.setTelephone(fournisseurDto.getTelephone());
        existingFournisseur.setAdresse(fournisseurDto.getAdresse());

        Fournisseur updatedFournisseur = create(existingFournisseur);
        return fournisseurMapper.toDto(updatedFournisseur);
    }

    public void deleteFournisseur(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Fournisseur non trouvé");
        }
        deleteById(id);
    }

    public List<FournisseurDto> searchByNom(String nom) {
        return repository.findByNomContainingIgnoreCase(nom).stream()
                .map(fournisseurMapper::toDto)
                .collect(Collectors.toList());
    }

}
