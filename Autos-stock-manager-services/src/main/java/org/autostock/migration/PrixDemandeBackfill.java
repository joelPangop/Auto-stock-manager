package org.autostock.migration;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reprise ponctuelle : avant la separation des deux prix, la colonne
 * prix_vente portait le prix affiche aux clients sur la vitrine. Sans ce
 * report, toutes les annonces publiques perdraient leur prix au deploiement.
 *
 * Les voitures VENDUE sont exclues : chez elles, prix_vente est bien un
 * montant encaisse, pas un prix demande.
 *
 * Une fois la reprise passee en production, cette classe peut etre supprimee.
 */
@Configuration
@Slf4j
public class PrixDemandeBackfill {

    @Bean
    ApplicationRunner reporterPrixVenteVersPrixDemande(EntityManager em) {
        return new ApplicationRunner() {
            @Override
            @Transactional
            public void run(ApplicationArguments args) {
                int reportes = em.createQuery(
                                "update Voiture v set v.prixDemande = v.prixVente "
                                        + "where v.prixDemande is null "
                                        + "and v.prixVente is not null "
                                        + "and v.statut <> org.autostock.enums.StatutVoiture.VENDUE")
                        .executeUpdate();
                if (reportes > 0) {
                    log.info("Prix demande repris depuis prix_vente pour {} voiture(s) non vendue(s)", reportes);
                }
            }
        };
    }
}
