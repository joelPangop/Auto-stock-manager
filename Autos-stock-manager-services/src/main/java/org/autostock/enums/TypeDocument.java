package org.autostock.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeDocument {

    FACTURE("Facture", "INVOICE"),
    IMMATRICULATION("Certificat d'immatriculation", "REGISTRATION"),
    INSPECTION("Rapport d'inspection", "INSPECTION"),
    CONTRAT("Contrat de vente", "CONTRACT"),
    AUTRE("Autre document", "OTHER");

    private final String label;
    private final String value;

    TypeDocument(String label, String value) {
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

    public static TypeDocument fromValue(String value) {
        for (TypeDocument t : values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Type de document inconnu : " + value);
    }
}
