package org.autostock.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MethodePaiement {
    COMPTANT("Comptant", "CASH"),
    CARTE("Carte bancaire", "CARD"),
    CHEQUE("Chèque", "CHEQUE"),
    VIREMENT("Virement bancaire", "TRANSFER"),
    FINANCEMENT("Financement", "LOAN");

    private final String label;   // lisible (frontend / UI)
    private final String value;   // technique (BD / API)

    MethodePaiement(String label, String value) {
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

    public static MethodePaiement fromValue(String value) {
        for (MethodePaiement m : values()) {
            if (m.value.equalsIgnoreCase(value)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Methode de paiement inconnue : " + value);
    }
}
