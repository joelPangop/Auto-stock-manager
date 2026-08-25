package org.autostock.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.persistence.EntityNotFoundException;
import org.autostock.cucumber.ScenarioContext;
import org.autostock.cucumber.TestAuthSupport;
import org.autostock.cucumber.TestDataFactory;
import org.autostock.enums.Role;
import org.autostock.enums.StatutVoiture;
import org.autostock.enums.TypeMouvement;
import org.autostock.models.User;
import org.autostock.models.Voiture;
import org.autostock.repositories.VoitureRepository;
import org.autostock.services.StockMouvementService;
import org.autostock.services.VoitureService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps pour voiture_stock.feature — règles métier de VoitureServiceImpl /
 * StockMouvementServiceImpl (statut initial, autorisation propriétaire/admin,
 * traçabilité des mouvements de stock à chaque transition).
 *
 * Ces steps (création d'utilisateurs, création de voiture, changement de
 * statut) sont réutilisées telles quelles par vente_paiement.feature via le
 * ScenarioContext partagé.
 */
public class VoitureStockSteps {

    @Autowired
    private TestDataFactory dataFactory;
    @Autowired
    private TestAuthSupport authSupport;
    @Autowired
    private VoitureService voitureService;
    @Autowired
    private StockMouvementService stockMouvementService;
    @Autowired
    private VoitureRepository voitureRepository;
    @Autowired
    private ScenarioContext context;

    @Given("un vendeur {string} propriétaire de véhicules")
    public void unVendeurProprietaire(String nom) {
        User u = dataFactory.creerUtilisateur(nom, Role.VENDEUR);
        context.getUtilisateurs().put(nom, u);
        authSupport.connecterEnTantQue(u);
    }

    @Given("une voiture {string} appartenant à {string} au prix de vente de {int}")
    public void uneVoitureAppartenantA(String nomVoiture, String proprietaire, int prix) throws Exception {
        User owner = context.getUtilisateurs().get(proprietaire);
        authSupport.connecterEnTantQue(owner);
        var marque = dataFactory.creerMarque("Honda");
        var modele = dataFactory.creerModele(marque, "Civic");
        Voiture v = new Voiture();
        v.setModele(modele);
        v.setVin("VIN-" + java.util.UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        v.setAnnee(2022);
        v.setCouleur("Noir");
        v.setPrixVente(BigDecimal.valueOf(prix));
        v.setPrixAchat(BigDecimal.valueOf(prix).multiply(BigDecimal.valueOf(0.8)));
        Voiture saved = voitureService.create(v);
        context.getVoitures().put(nomVoiture, saved);
    }

    @Given("un vendeur {string} qui n'est pas propriétaire de {string}")
    public void unVendeurNonProprietaire(String nom, String nomVoiture) {
        User u = dataFactory.creerUtilisateur(nom, Role.VENDEUR);
        context.getUtilisateurs().put(nom, u);
    }

    @Given("un administrateur {string}")
    public void unAdministrateur(String nom) {
        User u = dataFactory.creerUtilisateur(nom, Role.ADMIN);
        context.getUtilisateurs().put(nom, u);
    }

    @When("{string} change le statut de la voiture {string} à {string}")
    public void changeLeStatut(String nomUtilisateur, String nomVoiture, String statut) throws Exception {
        authSupport.connecterEnTantQue(context.getUtilisateurs().get(nomUtilisateur));
        if (statut.equals("HORS_SERVICE")) {
            context.setNbMouvementsAvantHorsService(
                    stockMouvementService.historiqueVoiture(context.getVoitures().get(nomVoiture).getId()).size());
        }
        Voiture maj = voitureService.changerStatut(context.getVoitures().get(nomVoiture).getId(), StatutVoiture.fromValue(statut));
        context.getVoitures().put(nomVoiture, maj);
    }

    @When("{string} tente de changer le statut de la voiture {string} à {string}")
    public void tenteDeChangerLeStatut(String nomUtilisateur, String nomVoiture, String statut) {
        authSupport.connecterEnTantQue(context.getUtilisateurs().get(nomUtilisateur));
        try {
            voitureService.changerStatut(context.getVoitures().get(nomVoiture).getId(), StatutVoiture.fromValue(statut));
        } catch (Exception e) {
            context.setDerniereErreur(e);
        }
    }

    @When("{string} tente de changer le statut d'une voiture inexistante à {string}")
    public void tenteDeChangerStatutVoitureInexistante(String nomUtilisateur, String statut) {
        authSupport.connecterEnTantQue(context.getUtilisateurs().get(nomUtilisateur));
        try {
            voitureService.changerStatut(-1L, StatutVoiture.fromValue(statut));
        } catch (Exception e) {
            context.setDerniereErreur(e);
        }
    }

    @Then("la voiture {string} a le statut {string}")
    public void laVoitureALeStatut(String nomVoiture, String statutAttendu) {
        Voiture v = voitureRepository.findById(context.getVoitures().get(nomVoiture).getId()).orElseThrow();
        assertThat(v.getStatut()).isEqualTo(StatutVoiture.fromValue(statutAttendu));
    }

    @Then("la voiture {string} a toujours le statut {string}")
    public void laVoitureAToujoursLeStatut(String nomVoiture, String statutAttendu) {
        laVoitureALeStatut(nomVoiture, statutAttendu);
    }

    @Then("l'historique de la voiture {string} contient un mouvement de type {string}")
    public void lHistoriqueContientUnMouvement(String nomVoiture, String type) {
        var historique = stockMouvementService.historiqueVoiture(context.getVoitures().get(nomVoiture).getId());
        assertThat(historique)
                .extracting(m -> m.getType())
                .contains(TypeMouvement.valueOf(type));
    }

    @Then("l'historique de la voiture {string} contient {int} mouvements de type {string}")
    public void lHistoriqueContientNMouvements(String nomVoiture, int nombreAttendu, String type) {
        var historique = stockMouvementService.historiqueVoiture(context.getVoitures().get(nomVoiture).getId());
        long occurrences = historique.stream().filter(m -> m.getType() == TypeMouvement.valueOf(type)).count();
        assertThat(occurrences).isEqualTo(nombreAttendu);
    }

    @Then("l'historique de la voiture {string} ne contient aucun nouveau mouvement depuis l'entrée en stock")
    public void lHistoriqueNeContientAucunNouveauMouvement(String nomVoiture) {
        var historique = stockMouvementService.historiqueVoiture(context.getVoitures().get(nomVoiture).getId());
        assertThat(historique).hasSize((int) context.getNbMouvementsAvantHorsService());
    }

    @Then("l'opération est refusée avec le message {string}")
    public void lOperationEstRefusee(String messageAttendu) {
        assertThat(context.getDerniereErreur()).isInstanceOf(AccessDeniedException.class);
        assertThat(context.getDerniereErreur().getMessage()).contains(messageAttendu);
    }

    @Then("l'opération échoue avec une erreur {string}")
    public void lOperationEchoueAvecUneErreur(String messageAttenduFragment) {
        assertThat(context.getDerniereErreur()).isInstanceOf(EntityNotFoundException.class);
        assertThat(context.getDerniereErreur().getMessage().toLowerCase()).contains(messageAttenduFragment.toLowerCase());
    }
}
