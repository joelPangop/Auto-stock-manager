package org.autostock.mappers;

import org.autostock.dtos.DepenseCreateDto;
import org.autostock.dtos.DepenseDto;
import org.autostock.models.*;
import org.springframework.stereotype.Component;

@Component
public class DepenseMapper {

    /**
     * Mapping entity -> dto (lecture).
     */
    public DepenseDto toDto(Depense d) {
        if (d == null) return null;

        DepenseDto dto = new DepenseDto();
        dto.setId(d.getId());

        dto.setVoitureId(d.getVoiture() != null ? d.getVoiture().getId() : null);
        dto.setEntretienId(d.getEntretien() != null ? d.getEntretien().getId() : null);
        dto.setDocumentId(d.getDocument() != null ? d.getDocument().getId() : null);

        if (d.getFournisseur() != null) {
            dto.setFournisseurId(d.getFournisseur().getId());
        } else {
            dto.setFournisseurId(null);
        }

        dto.setCategorie(d.getCategorie());
        dto.setMontant(d.getMontant());
        dto.setDateDepense(d.getDateDepense());
        dto.setDescription(d.getDescription());

        return dto;
    }

    /**
     * Mapping createDto -> entity.
     *
     * IMPORTANT: On passe les entités déjà chargées (refs) pour éviter de faire
     * des accès DB dans le mapper (bonne pratique).
     */
    public Depense toEntity(DepenseCreateDto dto,
                            Voiture voiture,
                            Entretien entretien,
                            Document document,
                            Fournisseur fournisseur) {

        if (dto == null) return null;
        if (voiture == null) {
            throw new IllegalArgumentException("voiture ne peut pas être null (voitureId obligatoire).");
        }

        Depense d = new Depense();
        d.setVoiture(voiture);         // obligatoire
        d.setEntretien(entretien);     // optionnel
        d.setDocument(document);       // optionnel
        d.setFournisseur(fournisseur); // optionnel

        d.setCategorie(dto.getCategorie());
        d.setMontant(dto.getMontant());
        d.setDateDepense(dto.getDateDepense());
        d.setDescription(dto.getDescription());

        return d;
    }

    /**
     * Mise à jour d'une dépense existante (optionnel mais pratique).
     * Tu l'utiliseras pour PUT/PATCH.
     */
    public void updateEntity(Depense d,
                             DepenseCreateDto dto,
                             Voiture voiture,
                             Entretien entretien,
                             Document document,
                             Fournisseur fournisseur) {

        if (d == null) throw new IllegalArgumentException("Depense target null.");
        if (dto == null) return;

        if (voiture != null) {
            d.setVoiture(voiture);
        }
        d.setEntretien(entretien);
        d.setDocument(document);
        d.setFournisseur(fournisseur);

        d.setCategorie(dto.getCategorie());
        d.setMontant(dto.getMontant());
        d.setDateDepense(dto.getDateDepense());
        d.setDescription(dto.getDescription());
    }
}
