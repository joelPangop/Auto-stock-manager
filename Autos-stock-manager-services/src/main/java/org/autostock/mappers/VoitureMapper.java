package org.autostock.mappers;

import org.autostock.dtos.VoitureCreateDto;
import org.autostock.dtos.VoitureDetailDto;
import org.autostock.dtos.VoitureListDto;
import org.autostock.models.Fournisseur;
import org.autostock.models.Modele;
import org.autostock.models.Voiture;
import org.springframework.stereotype.Component;

@Component
public class VoitureMapper {

    public Voiture toEntity(VoitureCreateDto dto, Modele modele, Fournisseur fournisseur) {
        Voiture v = new Voiture();
        v.setModele(modele);
        v.setFournisseur(fournisseur); // sera set par le service si nécessaire
        v.setAnnee(dto.getAnnee());
        v.setCouleur(dto.getCouleur());
        v.setVin(dto.getVin());
        v.setPrixAchat(dto.getPrixAchat());
        v.setPrixVente(dto.getPrixVente());
        return v;
    }

    public VoitureListDto toListDto(Voiture v) {
        VoitureListDto dto = new VoitureListDto();
        dto.setId(v.getId());
        dto.setMarque(v.getModele().getMarque().getNom());
        dto.setModele(v.getModele().getNom());
        dto.setAnnee(v.getAnnee());
        dto.setCouleur(v.getCouleur());
        dto.setVin(v.getVin());
        dto.setPrixVente(v.getPrixVente());
        dto.setStatut(v.getStatut().name());
        return dto;
    }

    public VoitureDetailDto toDetailDto(Voiture v) {
        VoitureDetailDto dto = new VoitureDetailDto();
        dto.setId(v.getId());
        dto.setIdModele(v.getModele().getId());
        dto.setIdFournisseur(
                v.getFournisseur() != null ? v.getFournisseur().getId() : null
        );
        dto.setMarque(v.getModele().getMarque().getNom());
        dto.setModele(v.getModele().getNom());
        dto.setAnnee(v.getAnnee());
        dto.setCouleur(v.getCouleur());
        dto.setVin(v.getVin());
        dto.setPrixAchat(v.getPrixAchat());
        dto.setPrixVente(v.getPrixVente());
        dto.setStatut(v.getStatut().name());
        dto.setDateEntreeStock(v.getDateEntreeStock());
        return dto;
    }
}
