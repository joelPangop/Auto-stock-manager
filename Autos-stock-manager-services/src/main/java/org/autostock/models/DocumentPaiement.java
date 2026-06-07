package org.autostock.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_paiement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentPaiement extends AbstractEntity {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_document", nullable = false)
    private Document document;

    @ManyToOne
    @JoinColumn(name = "id_paiement", nullable = false)
    private Paiement paiement;

    private byte[] content;

    private String mimeType;
}
