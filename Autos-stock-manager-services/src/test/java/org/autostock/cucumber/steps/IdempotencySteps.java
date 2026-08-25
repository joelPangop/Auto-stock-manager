package org.autostock.cucumber.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.autostock.services.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps pour idempotency.feature — règles métier de DempotencyServiceImp
 * (marquage d'un événement (consumer, eventId) comme déjà traité).
 */
public class IdempotencySteps {

    @Autowired
    private IdempotencyService idempotencyService;

    @When("l'événement {string} du consommateur {string} est marqué comme traité")
    public void lEvenementEstMarqueCommeTraite(String eventId, String consumer) {
        idempotencyService.markProcessed(consumer, eventId);
    }

    @Then("l'événement {string} du consommateur {string} est déjà traité")
    public void lEvenementEstDejaTraite(String eventId, String consumer) {
        assertThat(idempotencyService.alreadyProcessed(consumer, eventId)).isTrue();
    }

    @Then("l'événement {string} du consommateur {string} n'est pas déjà traité")
    public void lEvenementNEstPasDejaTraite(String eventId, String consumer) {
        assertThat(idempotencyService.alreadyProcessed(consumer, eventId)).isFalse();
    }
}
