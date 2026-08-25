package org.autostock.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires purs des enums métier utilisés dans les échanges API
 * (org.autostock.enums.*). Ces enums exposent une "valeur technique" via
 * @JsonValue (sérialisée en JSON / transmise par l'API) distincte du nom Java
 * de la constante, avec une méthode fromValue(String) utilisée par les
 * services pour convertir les entrées utilisateur. On vérifie ici que le
 * mapping est total et cohérent dans les deux sens, et que les valeurs
 * inconnues sont rejetées explicitement.
 */
class EnumsBusinessRulesTest {

    @ParameterizedTest
    @EnumSource(StatutVoiture.class)
    @DisplayName("StatutVoiture.fromValue(getValue()) est l'identité pour chaque constante")
    void statutVoiture_fromValue_estLIdentite(StatutVoiture statut) {
        assertThat(StatutVoiture.fromValue(statut.getValue())).isEqualTo(statut);
    }

    @Test
    @DisplayName("StatutVoiture.fromValue() est insensible à la casse")
    void statutVoiture_fromValue_insensibleALaCasse() {
        assertThat(StatutVoiture.fromValue("en_stock")).isEqualTo(StatutVoiture.EN_STOCK);
        assertThat(StatutVoiture.fromValue("Vendue")).isEqualTo(StatutVoiture.VENDUE);
    }

    @Test
    @DisplayName("StatutVoiture.fromValue() rejette une valeur inconnue")
    void statutVoiture_fromValue_valeurInconnue() {
        assertThatThrownBy(() -> StatutVoiture.fromValue("ARCHIVEE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ARCHIVEE");
    }

    @ParameterizedTest
    @EnumSource(TypeMouvement.class)
    @DisplayName("TypeMouvement.fromValue(getValue()) est l'identité pour chaque constante")
    void typeMouvement_fromValue_estLIdentite(TypeMouvement type) {
        assertThat(TypeMouvement.fromValue(type.getValue())).isEqualTo(type);
    }

    @Test
    @DisplayName("TypeMouvement.fromValue() rejette une valeur inconnue")
    void typeMouvement_fromValue_valeurInconnue() {
        assertThatThrownBy(() -> TypeMouvement.fromValue("TRANSFERT"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @EnumSource(MethodePaiement.class)
    @DisplayName("MethodePaiement.fromValue(getValue()) est l'identité pour chaque constante")
    void methodePaiement_fromValue_estLIdentite(MethodePaiement methode) {
        assertThat(MethodePaiement.fromValue(methode.getValue())).isEqualTo(methode);
    }

    @Test
    @DisplayName("MethodePaiement.fromValue() rejette une méthode inconnue")
    void methodePaiement_fromValue_methodeInconnue() {
        assertThatThrownBy(() -> MethodePaiement.fromValue("BITCOIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BITCOIN");
    }
}
