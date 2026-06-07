package org.autostock.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReceiptPdfServiceImp implements IReceiptPdfService {

    public byte[] generatePaiementReceipt(
            Long paiementId,
            String voitureLabel,
            BigDecimal montant,
            String methode,
            LocalDateTime date
    ) {
        try {
            Document doc = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);

            doc.open();

            doc.add(new Paragraph("REÇU DE PAIEMENT"));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Paiement #" + paiementId));
            doc.add(new Paragraph("Voiture : " + voitureLabel));
            doc.add(new Paragraph("Montant : " + montant + " $"));
            doc.add(new Paragraph("Méthode : " + methode));
//            doc.add(new Paragraph("Référence : " + (reference != null ? reference : "-")));
            doc.add(new Paragraph("Date : " + date));

            doc.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("Erreur génération reçu PDF", e);
        }
    }
}
