package org.autostock.dtos.client;

import org.autostock.models.Document;
import org.autostock.models.Voiture;

import java.math.BigDecimal;
import java.util.List;

public record VoiturePublicDetailDto(
        Long id,
        String marque,
        String modele,
        Integer annee,
        String couleur,
        Long kilometrage,
        // Alimente par prixDemande : c'est le prix affiche tant que la voiture
        // n'est pas vendue. Le nom du champ reste prixVente pour ne pas casser
        // le portail client, qui est deploye separement.
        BigDecimal prixVente,
        String statut,
        String vin,
        List<Long> photoIds,
        String description
) {
    public static VoiturePublicDetailDto from(Voiture v, List<Long> photoIds) {
        return new VoiturePublicDetailDto(
                v.getId(),
                v.getModele().getMarque().getNom(),
                v.getModele().getNom(),
                v.getAnnee(),
                v.getCouleur(),
                v.getKilometrage(),
                v.getPrixDemande(),
                v.getStatut().getValue(),
                v.getVin(),
                photoIds,
                v.getDescription()
        );
    }
}
