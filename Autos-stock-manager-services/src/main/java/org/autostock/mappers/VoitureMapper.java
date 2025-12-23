package org.autostock.mappers;

import org.autostock.dtos.VoitureCreateDto;
import org.autostock.dtos.VoitureDetailDto;
import org.autostock.dtos.VoitureListDto;
import org.autostock.dtos.VoitureUpdateDto;
import org.autostock.enums.StatutVoiture;
import org.autostock.models.Fournisseur;
import org.autostock.models.Modele;
import org.autostock.models.Voiture;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class VoitureMapper {

    public Voiture toEntity(VoitureCreateDto dto, Modele modele, Fournisseur fournisseur) {
        Voiture v = new Voiture();
        v.setModele(modele);
        v.setFournisseur(fournisseur); // sera set par le service si nécessaire
        v.setAnnee(dto.getAnnee());
        v.setCouleur(dto.getCouleur());
        v.setVin(dto.getVin());
        v.setKilometrage(dto.getKilometrage());
        v.setPrixAchat(dto.getPrixAchat());
        v.setPrixVente(dto.getPrixVente());
        return v;
    }

    public VoitureListDto toListDto(Voiture v) {
        VoitureListDto dto = new VoitureListDto();
        LocalDate threshold = LocalDate.now().minusMonths(3);
        boolean needsRemark = v.getDateEntreeStock() != null && !v.getDateEntreeStock().isAfter(threshold.atStartOfDay());
        dto.setId(v.getId());
        dto.setMarque(v.getModele().getMarque().getNom());
        dto.setModele(v.getModele().getNom());
        dto.setAnnee(v.getAnnee());
        dto.setCouleur(v.getCouleur());
        dto.setVin(v.getVin());
        dto.setPrixVente(v.getPrixVente());
        dto.setOwner(v.getOwner().getId());
        dto.setStatut(v.getStatut().name());
        dto.setNeedsRemark(needsRemark);
        return dto;
    }

    public VoitureDetailDto toDetailDto(Voiture v) {
        VoitureDetailDto dto = new VoitureDetailDto();
        LocalDate threshold = LocalDate.now().minusMonths(3);
        boolean needsRemark = v.getDateEntreeStock() != null && !v.getDateEntreeStock().isAfter(threshold.atStartOfDay());

        dto.setId(v.getId());
        dto.setIdModele(v.getModele().getId());
        dto.setIdFournisseur(
                v.getFournisseur() != null ? v.getFournisseur().getId() : null
        );
        dto.setIdMarque(v.getModele().getMarque().getId());
        dto.setAnnee(v.getAnnee());
        dto.setCouleur(v.getCouleur());
        dto.setVin(v.getVin());
        dto.setPrixAchat(v.getPrixAchat());
        dto.setKilometrage(v.getKilometrage());
        dto.setCreatedAt(v.getCreatedAt());
        dto.setUpdatedAt(v.getUpdatedAt());
        dto.setPrixVente(v.getPrixVente());
        dto.setStatut(v.getStatut().name());
        dto.setOwner(v.getOwner().getId());
        dto.setNeedsRemark(needsRemark);
        dto.setDateEntreeStock(v.getDateEntreeStock());
        return dto;
    }

    public Voiture toUpdatedEntity(VoitureUpdateDto dto, Modele modele, Fournisseur fournisseur) {
        Voiture v = new Voiture();
        v.setModele(modele);
        v.setFournisseur(fournisseur); // sera set par le service si nécessaire
        v.setAnnee(dto.getAnnee());
        v.setCouleur(dto.getCouleur());
        v.setVin(dto.getVin());
        v.setStatut(StatutVoiture.fromValue(dto.getStatut()));
        v.setKilometrage(dto.getKilometrage());
        v.setPrixAchat(dto.getPrixAchat());
        v.setPrixVente(dto.getPrixVente());
//        v.setOwner(dto.getOwner().getId());
        return v;
    }
}
