package org.autostock.mappers;

import org.autostock.dtos.FournisseurDto;
import org.autostock.models.Fournisseur;
import org.springframework.stereotype.Component;

@Component
public class FournisseurMapper {

    public FournisseurDto toDto(Fournisseur fournisseur) {
        FournisseurDto dto = new FournisseurDto();
        dto.setId(fournisseur.getId());
        dto.setNom(fournisseur.getNom());
        dto.setType(fournisseur.getType());
        dto.setTelephone(fournisseur.getTelephone());
        dto.setAdresse(fournisseur.getAdresse());
        return dto;
    }

    public Fournisseur toEntity(FournisseurDto dto) {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(dto.getId());
        fournisseur.setNom(dto.getNom());
        fournisseur.setType(dto.getType());
        fournisseur.setTelephone(dto.getTelephone());
        fournisseur.setAdresse(dto.getAdresse());
        return fournisseur;
    }
}
