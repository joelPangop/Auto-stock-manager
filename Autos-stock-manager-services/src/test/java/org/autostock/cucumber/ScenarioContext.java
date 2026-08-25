package org.autostock.cucumber;

import io.cucumber.spring.ScenarioScope;
import org.autostock.models.Client;
import org.autostock.models.User;
import org.autostock.models.Vente;
import org.autostock.models.Voiture;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * État partagé entre les différentes classes de steps pour la durée d'un seul
 * scénario. @ScenarioScope (cucumber-spring) fait qu'une nouvelle instance est
 * créée à chaque scénario et détruite à la fin — aucune fuite d'état entre
 * scénarios, même s'ils s'exécutent dans la même JVM.
 *
 * Important : ce bean étant scopé plus étroitement que les classes de steps
 * qui l'utilisent, Spring y injecte un proxy CGLIB (instancié via Objenesis,
 * sans exécuter les initialiseurs de champs). Accéder à un champ public
 * directement sur ce proxy lirait donc le champ (toujours null) du proxy
 * lui-même, pas celui de l'instance réelle du scénario — seuls les appels de
 * méthode sont interceptés et redirigés vers la bonne instance. D'où
 * l'exposition de l'état exclusivement via des méthodes (getters), jamais via
 * des champs publics.
 */
@Component
@ScenarioScope
public class ScenarioContext {

    private final Map<String, User> utilisateurs = new HashMap<>();
    private final Map<String, Voiture> voitures = new HashMap<>();
    private final Map<String, Client> clients = new HashMap<>();
    private final Map<String, Vente> ventes = new HashMap<>();

    private Exception derniereErreur;
    private long nbMouvementsAvantHorsService;

    public Map<String, User> getUtilisateurs() {
        return utilisateurs;
    }

    public Map<String, Voiture> getVoitures() {
        return voitures;
    }

    public Map<String, Client> getClients() {
        return clients;
    }

    public Map<String, Vente> getVentes() {
        return ventes;
    }

    public Exception getDerniereErreur() {
        return derniereErreur;
    }

    public void setDerniereErreur(Exception derniereErreur) {
        this.derniereErreur = derniereErreur;
    }

    public long getNbMouvementsAvantHorsService() {
        return nbMouvementsAvantHorsService;
    }

    public void setNbMouvementsAvantHorsService(long nbMouvementsAvantHorsService) {
        this.nbMouvementsAvantHorsService = nbMouvementsAvantHorsService;
    }
}
