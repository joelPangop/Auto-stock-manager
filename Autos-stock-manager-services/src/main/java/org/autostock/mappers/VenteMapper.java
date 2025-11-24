package org.autostock.mappers;

import org.autostock.dtos.VenteDto;
import org.autostock.models.Vente;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class VenteMapper {

    public VenteDto toDto(Vente v, BigDecimal totalPaye, BigDecimal resteAPayer) {
        VenteDto dto = new VenteDto();
        dto.setId(v.getId());
        dto.setIdVoiture(v.getVoiture().getId());
        dto.setVin(v.getVoiture().getVin());
        dto.setMarque(v.getVoiture().getModele().getMarque().getNom());
        dto.setModele(v.getVoiture().getModele().getNom());

        dto.setIdClient(v.getClient().getId());
        dto.setNomClient(v.getClient().getNom());

        dto.setIdVendeur(v.getVendeur().getId());
        dto.setNomVendeur(v.getVendeur().getNom());

        dto.setDateVente(v.getDateVente());
        dto.setPrixFinal(v.getPrixFinal());
        dto.setModePaiement(v.getModePaiement());

        dto.setTotalPaye(totalPaye);
        dto.setResteAPayer(resteAPayer);

        return dto;
    }
}
