package org.autostock.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SmsService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from-number:}")
    private String fromNumber;

    private boolean isConfigured() {
        return !accountSid.isBlank() && accountSid.startsWith("AC")
                && !authToken.isBlank() && authToken.length() >= 32
                && !fromNumber.isBlank() && fromNumber.startsWith("+")
                && !accountSid.contains("x") && !authToken.contains("x");
    }

    public void sendPasswordResetCode(String to, String code) {
        if (!isConfigured()) {
            throw new UnsupportedOperationException(
                    "Service SMS non configuré. Renseignez TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN et TWILIO_FROM_NUMBER dans le fichier .env"
            );
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        String credentials = Base64.getEncoder().encodeToString(
                (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("To", to);
        body.add("From", fromNumber);
        body.add("Body", "Auto Stock – Code de réinitialisation : " + code + ". Valide 15 minutes.");

        try {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<String> response = rest.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Échec d'envoi du SMS : " + response.getBody());
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Échec d'envoi du SMS (Twilio) : " + e.getStatusCode() + " — " + e.getResponseBodyAsString(), e);
        }
    }
}
