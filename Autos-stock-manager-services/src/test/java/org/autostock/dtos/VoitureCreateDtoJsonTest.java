package org.autostock.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le dialogue de creation envoie son formulaire tel quel. Toute valeur de
 * statut absente de {@code StatutVoiture} fait echouer la deserialisation
 * (400), et le front n'affiche qu'un "Erreur lors de l'enregistrement".
 */
class VoitureCreateDtoJsonTest {

    // Meme configuration que celle auto-configuree par Spring Boot.
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    private String payload(String statut) {
        return """
            {"idMarque":1,"idModele":1,"annee":2020,"vin":"1HGCM82633A004352","couleur":"Noir",
             "kilometrage":null,"prixAchat":null,"prixVente":null,
             "statut":"%s","idFournisseur":null,
             "dateEntreeStock":"2026-09-04","creerMouvementEntree":true}
            """.formatted(statut);
    }

    @ParameterizedTest(name = "statut {0}")
    @ValueSource(strings = {"EN_STOCK", "RESERVEE", "VENDUE", "HORS_SERVICE"})
    @DisplayName("tous les statuts proposes par le dialogue de creation sont acceptes")
    void statutsDuFrontAcceptes(String statut) throws Exception {
        VoitureCreateDto dto = mapper.readValue(payload(statut), VoitureCreateDto.class);
        assertEquals(1L, dto.getIdModele());
        assertNotNull(dto.getStatut());
    }

    @Test
    @DisplayName("DISPONIBLE n existe pas cote backend et doit rester hors du front")
    void disponibleRejete() {
        assertThrows(InvalidFormatException.class,
                () -> mapper.readValue(payload("DISPONIBLE"), VoitureCreateDto.class));
    }
}
