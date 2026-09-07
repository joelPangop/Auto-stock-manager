package org.autostock.mappers;

import org.autostock.dtos.VoitureCreateDto;
import org.autostock.dtos.VoitureUpdateDto;
import org.autostock.dtos.client.VoiturePublicDto;
import org.autostock.enums.StatutVoiture;
import org.autostock.models.Marque;
import org.autostock.models.Modele;
import org.autostock.models.Voiture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deux prix coexistent et ne doivent jamais se confondre :
 *  - prixDemande : affiche aux clients, saisi sur la fiche voiture ;
 *  - prixVente   : montant encaisse, renseigne par la vente uniquement.
 */
class VoitureMapperPrixTest {

    private final VoitureMapper mapper = new VoitureMapper();

    private Modele modele() {
        Marque marque = new Marque();
        marque.setNom("CHEVROLET");
        Modele m = new Modele();
        m.setNom("Equinox LTZ");
        m.setMarque(marque);
        return m;
    }

    @Test
    @DisplayName("la creation ne renseigne que le prix demande")
    void creationNeTouchePasAuPrixVente() {
        VoitureCreateDto dto = new VoitureCreateDto();
        dto.setPrixDemande(new BigDecimal("19500"));

        Voiture v = mapper.toEntity(dto, modele(), null);

        assertThat(v.getPrixDemande()).isEqualByComparingTo("19500");
        assertThat(v.getPrixVente())
                .as("une voiture non vendue n'a pas de prix de vente")
                .isNull();
    }

    @Test
    @DisplayName("la modification ne renseigne que le prix demande")
    void modificationNeTouchePasAuPrixVente() {
        VoitureUpdateDto dto = new VoitureUpdateDto();
        dto.setPrixDemande(new BigDecimal("18000"));
        dto.setStatut("EN_STOCK");

        Voiture v = mapper.toUpdatedEntity(dto, modele(), null);

        assertThat(v.getPrixDemande()).isEqualByComparingTo("18000");
        assertThat(v.getPrixVente())
                .as("le prix de vente est repris de la base par le service")
                .isNull();
    }

    @Test
    @DisplayName("la vitrine affiche le prix demande, pas le prix encaisse")
    void vitrineAfficheLePrixDemande() {
        Voiture v = new Voiture();
        v.setId(3L);
        v.setModele(modele());
        v.setStatut(StatutVoiture.EN_STOCK);
        v.setPrixDemande(new BigDecimal("19500"));
        v.setPrixVente(null);

        VoiturePublicDto dto = VoiturePublicDto.from(v, null);

        assertThat(dto.prixVente())
                .as("le champ expose au portail client porte le prix demande")
                .isEqualByComparingTo("19500");
    }
}
