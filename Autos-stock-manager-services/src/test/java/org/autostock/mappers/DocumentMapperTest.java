package org.autostock.mappers;

import org.autostock.enums.TypeDocument;
import org.autostock.models.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * L'ecriture d'un document resout son type avec TypeDocument.valueOf(), donc
 * en nom de constante. La lecture doit parler la meme langue, sinon un
 * document enregistre en FACTURE ressort sous un autre identifiant et aucun
 * client ne sait le reclasser.
 */
class DocumentMapperTest {

    private final DocumentMapper mapper = new DocumentMapper();

    private Document documentDeType(TypeDocument type) {
        Document d = new Document();
        d.setType(type);
        return d;
    }

    @ParameterizedTest
    @EnumSource(TypeDocument.class)
    @DisplayName("le type expose est relisible par valueOf, quel que soit le type")
    void typeRelisibleParValueOf(TypeDocument type) {
        String expose = mapper.toDto(documentDeType(type)).getType();

        assertEquals(type, TypeDocument.valueOf(expose),
                "le type renvoye doit pouvoir etre renvoye tel quel a l'ecriture");
    }

    @Test
    @DisplayName("FACTURE ne ressort pas en INVOICE")
    void factureResteFacture() {
        assertEquals("FACTURE", mapper.toDto(documentDeType(TypeDocument.FACTURE)).getType());
    }

    @Test
    @DisplayName("le libelle lisible reste disponible a part")
    void libelleConserve() {
        var dto = mapper.toDto(documentDeType(TypeDocument.IMMATRICULATION));

        assertEquals("IMMATRICULATION", dto.getType());
        assertEquals("Certificat d'immatriculation", dto.getTypeLabel());
    }
}
