# Tests automatisés — Autos-stock-manager-services

Ce module contient deux familles de tests, toutes deux basées sur JUnit 5 :

1. **Tests unitaires** (`src/test/java/org/autostock/**/*Test.java`, hors package `cucumber`)
   Mockito, aucune base de données, aucun contexte Spring — rapides, isolés,
   testent la logique d'un service en mockant ses dépendances.

2. **Tests d'acceptation BDD (Cucumber)** (`src/test/resources/features/*.feature`
   + `src/test/java/org/autostock/cucumber/**`)
   Contexte Spring Boot complet (`@SpringBootTest`), base H2 en mémoire créée à
   la volée. Chaque scénario s'exécute dans sa propre transaction, annulée
   automatiquement à la fin — aucun nettoyage manuel requis, aucune fuite
   d'état entre scénarios.

Les deux familles ont été construites en déduisant les règles métier
directement du code existant (services, entités, enums) — voir le
commentaire d'en-tête de chaque fichier `.feature` pour la liste des règles
couvertes et leur source.

## Périmètre couvert actuellement

Trois modules métier centraux, choisis comme point de départ :

- **Voiture / Stock** (`voiture_stock.feature`, `VoitureServiceImplTest`) :
  statut initial à la création, autorisation propriétaire/admin sur le
  changement de statut, traçabilité des mouvements de stock.
- **Vente / Paiement** (`vente_paiement.feature`, `PaiementServiceImplTest`) :
  création d'une vente, calcul du total payé et du reste à payer, validation
  de la méthode de paiement.
- **Idempotency** (`idempotency.feature`) : marquage d'un événement
  `(consumer, eventId)` comme déjà traité.

Certains scénarios documentent volontairement des comportements actuels du
code qui ressemblent à des lacunes (ex. absence de contrôle de sur-paiement,
double mouvement `VENTE` enregistré à la création d'une vente, transition
`HORS_SERVICE` non tracée). Le but est d'empêcher toute régression silencieuse
tant que ces points n'ont pas été tranchés côté produit — pas de les valider
comme souhaitables.

Modules non encore couverts (à ajouter dans une itération suivante) :
Client, Fournisseur, Dépense, Entretien, Document/S3, Dashboard, Audit.

## Règles fonctionnelles (RF)

Chaque RF est formulée en Quand / Alors, **exécutable** : le scénario Cucumber
qui la vérifie porte le tag `@RF-XX` correspondant (en plus de ses tags de
module), donc chaque règle peut être lancée isolément :

```bash
./mvnw test -Dtest=RunCucumberTest -Dcucumber.filter.tags="@RF-05"

# Plusieurs RF à la fois
./mvnw test -Dtest=RunCucumberTest -Dcucumber.filter.tags="@RF-05 or @RF-10"
```

Référence ci-dessous : `feature:ligne` = emplacement du scénario, source
déduite du code entre parenthèses.

### Voiture / Stock (`voiture_stock.feature`)

- **RF-01** — Quand une voiture est créée, Alors elle est automatiquement au
  statut `EN_STOCK` et un mouvement `ENTREE` est tracé.
  (`VoitureServiceImpl.create` — *L'ajout d'une voiture au stock est tracé*, ligne 13)
- **RF-02** — Quand le propriétaire change le statut de sa voiture vers
  `RESERVEE` ou `VENDUE`, Alors le statut est mis à jour et un mouvement
  `RESERVATION`/`VENTE` respectivement est tracé.
  (`VoitureServiceImpl.changerStatut` — *Le propriétaire fait évoluer le statut…*, ligne 17)
- **RF-03** — Quand une voiture `RESERVEE` repasse à `EN_STOCK`, Alors un
  mouvement `RETOUR` est tracé.
  (`VoitureServiceImpl.changerStatut` — *Annuler une réservation…*, ligne 27)
- **RF-04** — Quand une voiture passe à `HORS_SERVICE`, Alors aucun mouvement
  de stock n'est enregistré (lacune actuelle du code, non un choix voulu).
  (`VoitureServiceImpl.changerStatut` — *Passer une voiture Hors Service…*, ligne 33)
- **RF-05** — Quand un utilisateur qui n'est ni propriétaire ni admin tente de
  changer le statut d'une voiture, Alors l'opération est refusée avec le
  message *"Vous n'êtes pas propriétaire"* et le statut reste inchangé.
  (`VoitureServiceImpl.changerStatut` — *Un utilisateur non propriétaire…*, ligne 40)
- **RF-06** — Quand un administrateur change le statut d'une voiture dont il
  n'est pas propriétaire, Alors l'opération est autorisée.
  (`VoitureServiceImpl.changerStatut` — *Un administrateur peut changer…*, ligne 46)
- **RF-07** — Quand on tente de changer le statut d'une voiture inexistante,
  Alors l'opération échoue avec une erreur *"voiture introuvable"*.
  (`VoitureServiceImpl.changerStatut` — *Changer le statut d'une voiture inexistante…*, ligne 51)

### Vente / Paiement (`vente_paiement.feature`)

- **RF-08** — Quand une vente est créée pour une voiture `EN_STOCK`, Alors la
  voiture passe au statut `VENDUE`.
  (`VenteServiceImpl.creerVente` — *Créer une vente marque la voiture comme vendue*, ligne 16)
- **RF-09** — Quand une vente est créée, Alors deux mouvements de type `VENTE`
  sont enregistrés (redondance actuelle : `creerVente()` en trace un
  explicitement en plus de celui déjà généré par `changerStatut(VENDUE)`).
  (`VenteServiceImpl.creerVente` — *La création d'une vente enregistre deux mouvements…*, ligne 22)
- **RF-10** — Quand un vendeur qui n'est pas propriétaire de la voiture tente
  de la vendre, Alors la vente échoue avec le message *"Vous n'êtes pas
  propriétaire"* (règle héritée de RF-05 via la délégation à `changerStatut`).
  (`VenteServiceImpl.creerVente` — *Un vendeur ne peut pas vendre…*, ligne 29)
- **RF-11** — Quand un paiement est ajouté à une vente, Alors le total payé
  augmente d'autant et le reste à payer (`prixFinal - somme des paiements`)
  diminue d'autant.
  (`PaiementServiceImpl.ajouterPaiement` — *Chaque paiement enregistré augmente…*, ligne 35)
- **RF-12** — Quand un paiement est ajouté avec une méthode de paiement
  inconnue, Alors il est rejeté avec l'erreur *"Methode de paiement inconnue"*.
  (`PaiementServiceImpl.ajouterPaiement` — *Un paiement avec une méthode inconnue…*, ligne 42)
- **RF-13** — Quand un paiement dépasse le reste à payer, Alors il est accepté
  quand même et le reste à payer devient négatif (aucun contrôle de
  sur-paiement actuellement).
  (`PaiementServiceImpl.ajouterPaiement` — *Le système ne bloque pas un paiement supérieur…*, ligne 47)

### Idempotence (`idempotency.feature`)

- **RF-14** — Quand aucun événement n'a été marqué pour la paire
  `(consumer, eventId)`, Alors il n'est pas considéré comme déjà traité.
  (`IdempotencyService.alreadyProcessed` — *Un événement jamais marqué…*, ligne 9)
- **RF-15** — Quand un événement est marqué comme traité, Alors il est
  considéré comme déjà traité pour la même paire `(consumer, eventId)`.
  (`IdempotencyService.markProcessed` — *Marquer un événement comme traité…*, ligne 12)
- **RF-16** — Quand un événement est marqué pour un consommateur donné, Alors
  le même `eventId` reste "non traité" pour un autre consommateur, et un autre
  `eventId` reste "non traité" pour le même consommateur (le marquage est
  spécifique à la paire complète).
  (`IdempotencyService` — *Le même identifiant… / Un autre identifiant…*, lignes 16 et 20)
- **RF-17** — Quand le même événement est marqué deux fois, Alors aucune
  erreur n'est levée (absence de contrainte d'unicité en base sur
  `(consumer_name, event_id)`).
  (`DempotencyServiceImp.markProcessed` — *Marquer deux fois le même événement…*, ligne 24)

## Lancer les tests

```bash
# Tout (unitaires + Cucumber)
./mvnw test

# Uniquement les scénarios Cucumber
./mvnw test -Dtest=RunCucumberTest

# Uniquement un sous-ensemble de scénarios par tag
./mvnw test -Dcucumber.filter.tags="@vente"

# Rapport de couverture (JaCoCo, déjà configuré dans le pom.xml)
./mvnw verify
# -> target/site/jacoco/index.html
```

Le rapport HTML Cucumber est généré dans `target/cucumber-report/report.html`.

## Structure

```
src/test/java/org/autostock/
├── cucumber/
│   ├── CucumberSpringConfiguration.java   # glue Spring + @Transactional (rollback par scénario)
│   ├── RunCucumberTest.java                # point d'entrée JUnit 5 (Suite)
│   ├── Hooks.java                          # @Before/@After (sécurité)
│   ├── ScenarioContext.java                # état partagé entre steps, scoping "cucumber-glue"
│   ├── TestAuthSupport.java                # simule l'utilisateur authentifié (SecurityContextHolder)
│   ├── TestDataFactory.java                # construction des entités JPA de test
│   └── steps/
│       ├── VoitureStockSteps.java
│       ├── VentePaiementSteps.java
│       └── IdempotencySteps.java
├── services/
│   ├── VoitureServiceImplTest.java         # unitaire (Mockito)
│   └── PaiementServiceImplTest.java        # unitaire (Mockito)
└── enums/
    └── EnumsBusinessRulesTest.java         # unitaire (mapping fromValue/getValue)

src/test/resources/
├── application.properties                  # config de test (H2, remplace intégralement le fichier de src/main)
├── schema.sql                              # table technique kafka_processed_event (hors JPA)
└── features/
    ├── voiture_stock.feature
    ├── vente_paiement.feature
    └── idempotency.feature
```

## Pourquoi des tests "de service" plutôt que "API" (HTTP)

Les scénarios Cucumber appellent directement les services Spring
(`VoitureService`, `VenteService`, `PaiementService`, `IdempotencyService`)
plutôt que de passer par `MockMvc`/HTTP. C'est plus rapide, isole les règles
métier de la couche contrôleur/sécurité HTTP, et évite de dupliquer les
`@Value` requis par des modules hors périmètre (S3, SES, SQS, mail — leurs
beans sont construits au démarrage du contexte mais jamais appelés par ces
scénarios). Si une couverture bout-en-bout (contrôleurs + sécurité JWT + CORS)
est souhaitée plus tard, elle peut être ajoutée comme un deuxième runner
Cucumber avec `@AutoConfigureMockMvc`, en parallèle de celui-ci.
