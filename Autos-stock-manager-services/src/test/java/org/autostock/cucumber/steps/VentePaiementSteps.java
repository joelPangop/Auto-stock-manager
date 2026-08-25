package org.autostock.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.autostock.cucumber.ScenarioContext;
import org.autostock.cucumber.TestAuthSupport;
import org.autostock.cucumber.TestDataFactory;
import org.autostock.models.Client;
import org.autostock.models.Vente;
import org.autostock.services.PaiementService;
import org.autostock.services.VenteService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps pour vente_paiement.feature — règles métier de VenteServiceImpl /
 * PaiementServiceImpl (statut voiture à la vente, calcul total payé / reste à
 * payer, validation de la méthode de paiement).
 *
 * Réutilise le ScenarioContext et les steps de création d'utilisateurs /
 * voitures définis dans VoitureStockSteps (même glue package, backgrounds
 * communs).
 */
public class VentePaiementSteps {

    @Autowired
    private TestDataFactory dataFactory;
    @Autowired
    private TestAuthSupport authSupport;
    @Autowired
    private VenteService venteService;
    @Autowired
    private PaiementService paiementService;
    @Autowired
    private ScenarioContext context;

    @Given("un client {string}")
    public void unClient(String nom) {
        Client c = dataFactory.creerClient(nom);
        context.getClients().put(nom, c);
    }

    @When("{string} vend la voiture {string} au client {string} pour {int} payé par {string}")
    public void vendLaVoiture(String nomVendeur, String nomVoiture, String nomClient, int prix, String methode) throws Exception {
        authSupport.connecterEnTantQue(context.getUtilisateurs().get(nomVendeur));
        Vente vente = venteService.creerVente(
                context.getVoitures().get(nomVoiture).getId(),
                context.getClients().get(nomClient).getId(),
                context.getUtilisateurs().get(nomVendeur).getId(),
                BigDecimal.valueOf(prix),
                methode
        );
        context.getVentes().put(nomVoiture, vente);
    }

    @When("{string} tente de vendre la voiture {string} au client {string} pour {int} payé par {string}")
    public void tenteDeVendreLaVoiture(String nomVendeur, String nomVoiture, String nomClient, int prix, String methode) {
        authSupport.connecterEnTantQue(context.getUtilisateurs().get(nomVendeur));
        try {
            Vente vente = venteService.creerVente(
                    context.getVoitures().get(nomVoiture).getId(),
                    context.getClients().get(nomClient).getId(),
                    context.getUtilisateurs().get(nomVendeur).getId(),
                    BigDecimal.valueOf(prix),
                    methode
            );
            context.getVentes().put(nomVoiture, vente);
        } catch (Exception e) {
            context.setDerniereErreur(e);
        }
    }

    @When("un paiement de {int} par {string} est ajouté à la vente de {string}")
    public void unPaiementEstAjoute(int montant, String methode, String nomVoiture) {
        Long idVente = context.getVentes().get(nomVoiture).getId();
        paiementService.ajouterPaiement(idVente, BigDecimal.valueOf(montant), methode);
    }

    @When("une tentative de paiement de {int} par {string} est faite sur la vente de {string}")
    public void uneTentativeDePaiementEstFaite(int montant, String methode, String nomVoiture) {
        Long idVente = context.getVentes().get(nomVoiture).getId();
        try {
            paiementService.ajouterPaiement(idVente, BigDecimal.valueOf(montant), methode);
        } catch (Exception e) {
            context.setDerniereErreur(e);
        }
    }

    @Then("le total payé pour la vente de {string} est {int}")
    public void leTotalPayeEst(String nomVoiture, int montantAttendu) {
        Long idVente = context.getVentes().get(nomVoiture).getId();
        assertThat(paiementService.totalPaye(idVente)).isEqualByComparingTo(BigDecimal.valueOf(montantAttendu));
    }

    @Then("le reste à payer pour la vente de {string} est {int}")
    public void leResteAPayerEst(String nomVoiture, int montantAttendu) {
        Long idVente = context.getVentes().get(nomVoiture).getId();
        assertThat(paiementService.resteAPayer(idVente)).isEqualByComparingTo(BigDecimal.valueOf(montantAttendu));
    }

    @Then("la vente échoue avec le message {string}")
    public void laVenteEchoueAvecLeMessage(String messageAttendu) {
        assertThat(context.getDerniereErreur()).isInstanceOf(AccessDeniedException.class);
        assertThat(context.getDerniereErreur().getMessage()).contains(messageAttendu);
    }

    @Then("le paiement échoue avec une erreur {string}")
    public void lePaiementEchoueAvecUneErreur(String messageAttenduFragment) {
        assertThat(context.getDerniereErreur()).isInstanceOf(IllegalArgumentException.class);
        assertThat(context.getDerniereErreur().getMessage()).contains(messageAttenduFragment);
    }
}
