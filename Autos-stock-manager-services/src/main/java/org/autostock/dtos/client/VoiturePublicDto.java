package org.autostock.dtos.client;

import org.autostock.models.Voiture;

import java.math.BigDecimal;

public record VoiturePublicDto(
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
        Long photoId   // ID du premier document de type PHOTO, null si aucun
) {
    public static VoiturePublicDto from(Voiture v, Long photoId) {
        return new VoiturePublicDto(
                v.getId(),
                v.getModele().getMarque().getNom(),
                v.getModele().getNom(),
                v.getAnnee(),
                v.getCouleur(),
                v.getKilometrage(),
                v.getPrixDemande(),
                v.getStatut().getValue(),
                photoId
        );
    }
}
