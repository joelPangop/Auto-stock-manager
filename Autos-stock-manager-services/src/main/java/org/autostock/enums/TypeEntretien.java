package org.autostock.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeEntretien {

    VIDANGE("Vidange d'huile", "OIL_CHANGE"),
    FREINS("Remplacement des freins", "BRAKE_SERVICE"),
    PNEUS("Changement de pneus", "TIRE_CHANGE"),
    INSPECTION("Inspection générale", "INSPECTION"),
    DIAGNOSTIC("Diagnostic moteur/électronique", "DIAGNOSTIC"),
    AUTRE("Autre entretien", "OTHER");

    private final String label;  // lisible en UI
    private final String value;  // stocké/API

    TypeEntretien(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static TypeEntretien fromValue(String value) {
        for (TypeEntretien t : values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Type d'entretien inconnu : " + value);
    }
}
