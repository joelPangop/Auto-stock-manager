package org.autostock.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.Map;

/**
 * Service de notifications asynchrones via AWS SQS (ou LocalStack en dev).
 * Activé quand aws.sqs.notifications-url est défini.
 *
 * Usages :
 *  - Notification de vente conclue
 *  - Rappel d'entretien à venir
 *  - Alerte stock bas
 */
@Service
@ConditionalOnProperty(name = "aws.sqs.notifications-url")
@Slf4j
public class SqsNotificationService {

    private final SqsClient sqs;
    private final ObjectMapper mapper;

    @Value("${aws.sqs.notifications-url}")
    private String queueUrl;

    public SqsNotificationService(SqsClient sqs, ObjectMapper mapper) {
        this.sqs = sqs;
        this.mapper = mapper;
    }

    // -------------------------------------------------------------------------
    // Envoi de messages
    // -------------------------------------------------------------------------

    public void notifierVente(Long venteId, String nomClient, String modele, double montant) {
        envoyer("VENTE_CONCLUE", Map.of(
                "venteId", venteId,
                "nomClient", nomClient,
                "modele", modele,
                "montant", montant
        ));
    }

    public void notifierEntretienAVenir(Long voitureId, String modele, String dateEntretien) {
        envoyer("ENTRETIEN_A_VENIR", Map.of(
                "voitureId", voitureId,
                "modele", modele,
                "dateEntretien", dateEntretien
        ));
    }

    public void notifierStockBas(String modele, int quantite) {
        envoyer("STOCK_BAS", Map.of(
                "modele", modele,
                "quantite", quantite
        ));
    }

    // -------------------------------------------------------------------------
    // Consommation de messages (polling)
    // -------------------------------------------------------------------------

    public List<Message> lireMessages(int maxMessages) {
        var response = sqs.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(Math.min(maxMessages, 10))
                .waitTimeSeconds(5) // long polling
                .build());
        return response.messages();
    }

    public void acquitter(String receiptHandle) {
        sqs.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build());
    }

    // -------------------------------------------------------------------------
    // Interne
    // -------------------------------------------------------------------------

    private void envoyer(String type, Map<String, Object> payload) {
        try {
            Map<String, Object> message = Map.of("type", type, "payload", payload);
            String body = mapper.writeValueAsString(message);
            sqs.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .messageAttributes(Map.of(
                            "type", MessageAttributeValue.builder()
                                    .dataType("String")
                                    .stringValue(type)
                                    .build()
                    ))
                    .build());
            log.info("[SQS] Message envoyé : type={}", type);
        } catch (Exception e) {
            // Non bloquant — on log sans faire échouer l'opération principale
            log.warn("[SQS] Échec envoi notification {} : {}", type, e.getMessage());
        }
    }
}
