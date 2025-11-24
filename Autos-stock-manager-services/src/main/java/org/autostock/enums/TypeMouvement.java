package org.autostock.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TypeMouvement {

    ENTREE("Entrée en stock", "IN"),
    SORTIE("Sortie du stock", "OUT"),
    VENTE("Vente effectuée", "SALE"),
    RETOUR("Retour au stock", "RETURN"),
    RESERVATION("Réservation", "RESERVE");

    private final String label;  // lisible par l'utilisateur
    private final String value;  // stocké / transmis par API

    TypeMouvement(String label, String value) {
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

    public static TypeMouvement fromValue(String value) {
        for (TypeMouvement t : values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Type de mouvement inconnu : " + value);
    }
}
