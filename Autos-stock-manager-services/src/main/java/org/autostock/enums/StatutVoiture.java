package org.autostock.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatutVoiture {
    EN_STOCK("En Stock", "EN_STOCK"),
    VENDUE("Vendue", "VENDUE"),
    RESERVEE("Reservee", "RESERVEE"),
    HORS_SERVICE("Hors Service", "HORS_SERVICE");

    private final String label;
    private final String value;

    StatutVoiture(String label, String value) {
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

    public static StatutVoiture fromValue(String value) {
        for (StatutVoiture t : values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Statut Voiture inconnu : " + value);
    }

}
