package org.autostock.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CategorieDepense {
    ENTRETIEN("Entretien", "ENTRETIEN"),
    REPARATION("Reparation", "REPARATION"),
    ASSURANCE("Assurance", "ASSURANCE"),
    ESSENCE("Essence", "ESSENCE"),
    FRAIS_ADMIN("Frais d'administration", "FRAIS_ADMIN"),
    AMENDE("Amende", "AMENDE"),
    PARKING("Parking", "PARKING"),
    LAVAGE("Lavage", "LAVAGE"),
    AUTRE("Autre", "AUTRE");

    private final String label;   // lisible (frontend / UI)
    private final String value;   // technique (BD / API)

    CategorieDepense(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    @JsonValue  // important : utilisé lors du retour JSON
    public String getValue() {
        return value;
    }

    public static CategorieDepense fromValue(String value) {
        for (CategorieDepense m : values()) {
            if (m.value.equalsIgnoreCase(value)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Categorie de Depense inconnue : " + value);
    }
}
