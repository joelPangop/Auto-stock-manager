# language: fr
# Règles métier couvertes (déduites de VoitureServiceImpl / StockMouvementServiceImpl) :
#  - Une voiture nouvellement créée est automatiquement EN_STOCK et génère un mouvement ENTREE.
#  - Seuls le propriétaire de la voiture ou un administrateur peuvent changer son statut.
#  - Chaque transition de statut génère un mouvement de stock tracé (sauf HORS_SERVICE, non géré).
@voiture @stock
Fonctionnalité: Cycle de vie d'une voiture dans le stock

  Contexte:
    Étant donné un vendeur "Alice" propriétaire de véhicules
    Et une voiture "Civic 2022" appartenant à "Alice" au prix de vente de 25000

  @RF-01
  Scénario: L'ajout d'une voiture au stock est tracé
    Alors la voiture "Civic 2022" a le statut "EN_STOCK"
    Et l'historique de la voiture "Civic 2022" contient un mouvement de type "ENTREE"

  @RF-02
  Plan du scénario: Le propriétaire fait évoluer le statut de sa voiture et chaque transition est tracée
    Quand "Alice" change le statut de la voiture "Civic 2022" à "<nouveauStatut>"
    Alors la voiture "Civic 2022" a le statut "<nouveauStatut>"
    Et l'historique de la voiture "Civic 2022" contient un mouvement de type "<mouvementAttendu>"

    Exemples:
      | nouveauStatut | mouvementAttendu |
      | RESERVEE      | RESERVATION       |
      | VENDUE        | VENTE             |

  @RF-03
  Scénario: Annuler une réservation remet la voiture en stock et trace un retour
    Quand "Alice" change le statut de la voiture "Civic 2022" à "RESERVEE"
    Et "Alice" change le statut de la voiture "Civic 2022" à "EN_STOCK"
    Alors la voiture "Civic 2022" a le statut "EN_STOCK"
    Et l'historique de la voiture "Civic 2022" contient un mouvement de type "RETOUR"

  @RF-04
  Scénario: Passer une voiture Hors Service ne génère aucun mouvement de stock
    # Comportement actuel du code : le switch de changerStatut() ne couvre pas HORS_SERVICE.
    # Ce scénario documente ce comportement pour éviter une régression silencieuse s'il change.
    Quand "Alice" change le statut de la voiture "Civic 2022" à "HORS_SERVICE"
    Alors la voiture "Civic 2022" a le statut "HORS_SERVICE"
    Et l'historique de la voiture "Civic 2022" ne contient aucun nouveau mouvement depuis l'entrée en stock

  @RF-05
  Scénario: Un utilisateur non propriétaire et non admin ne peut pas changer le statut
    Étant donné un vendeur "Bruno" qui n'est pas propriétaire de "Civic 2022"
    Quand "Bruno" tente de changer le statut de la voiture "Civic 2022" à "RESERVEE"
    Alors l'opération est refusée avec le message "Vous n’êtes pas propriétaire"
    Et la voiture "Civic 2022" a toujours le statut "EN_STOCK"

  @RF-06
  Scénario: Un administrateur peut changer le statut d'une voiture dont il n'est pas propriétaire
    Étant donné un administrateur "Carole"
    Quand "Carole" change le statut de la voiture "Civic 2022" à "RESERVEE"
    Alors la voiture "Civic 2022" a le statut "RESERVEE"

  @RF-07
  Scénario: Changer le statut d'une voiture inexistante échoue
    Quand "Alice" tente de changer le statut d'une voiture inexistante à "RESERVEE"
    Alors l'opération échoue avec une erreur "voiture introuvable"
