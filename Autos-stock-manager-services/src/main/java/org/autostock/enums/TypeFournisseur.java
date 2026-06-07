package org.autostock.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeFournisseur {
    CONCESSION("Consessionnaire", "CONCESSION"),
    PARTICULIER("Particulier", "PARTICULIER"),
    ADMINISTRATION("Administration", "ADMINISTRATION"),
    ENTRETIEN("Entretien", "ENTRETIEN"),
    AUTRE("Autre", "AUTRE");

    private final String label;
    private final String value;

    TypeFournisseur(String label, String value) {
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

    public static TypeFournisseur fromValue(String value) {
        for (TypeFournisseur t : values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Statut Voiture inconnu : " + value);
    }

}
