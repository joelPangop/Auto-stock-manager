package org.autostock.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CategorieVoiture {
    CITADINE("Citadine", "CITADINE"),
    BERLINE("Berline", "BERLINE"),
    SUV_4X4("SUV / 4x4", "SUV_4X4"),
    PREMIUM("Premium", "PREMIUM");

    private final String label;
    private final String value;

    CategorieVoiture(String label, String value) {
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

    public static CategorieVoiture fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        for (CategorieVoiture c : values()) {
            if (c.value.equalsIgnoreCase(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Categorie Voiture inconnue : " + value);
    }
}
