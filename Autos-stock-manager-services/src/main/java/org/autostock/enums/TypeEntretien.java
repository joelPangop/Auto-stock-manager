package org.autostock.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeEntretien {

    VIDANGE("Vidange d'huile", "VIDANGE"),
    FREINS("Remplacement des freins", "FREINS"),
    PNEUS("Changement de pneus", "PNEUS"),
    INSPECTION("Inspection générale", "INSPECTION"),
    DIAGNOSTIC("Diagnostic moteur/électronique", "DIAGNOSTIC"),
    ELECTRIQUE("Problème électrique", "ELECTRIQUE"),
    AUTRE("Autre entretien", "AUTRE");

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
