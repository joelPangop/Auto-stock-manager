package org.autostock.services;

import org.autostock.dtos.FournisseurDto;
import org.autostock.models.Fournisseur;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface FournisseurService extends IService<Fournisseur, Long> {

List<FournisseurDto> getAllFournisseurs();
    FournisseurDto getFournisseurById(Long id);
    FournisseurDto createFournisseur(FournisseurDto fournisseurDto) throws AccessDeniedException;
    FournisseurDto updateFournisseur(Long id, FournisseurDto fournisseurDto) throws AccessDeniedException;
    void deleteFournisseur(Long id);
    List<FournisseurDto> searchByNom(String nom);
}
