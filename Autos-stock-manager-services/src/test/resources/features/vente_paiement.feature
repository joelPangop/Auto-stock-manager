# language: fr
# Règles métier couvertes (déduites de VenteServiceImpl / PaiementServiceImpl) :
#  - Créer une vente marque la voiture VENDUE et trace un mouvement de stock.
#  - creerVente() délègue le changement de statut à VoitureService.changerStatut(),
#    donc la même règle d'autorisation propriétaire/admin s'applique implicitement.
#  - Le reste à payer = prixFinal - somme des paiements enregistrés.
#  - Une méthode de paiement inconnue est rejetée.
@vente @paiement
Fonctionnalité: Vente d'une voiture et suivi des paiements

  Contexte:
    Étant donné un vendeur "Diane" propriétaire de véhicules
    Et une voiture "Civic 2022" appartenant à "Diane" au prix de vente de 25000
    Et un client "Marc Client"

  @RF-08
  Scénario: Créer une vente marque la voiture comme vendue
    Quand "Diane" vend la voiture "Civic 2022" au client "Marc Client" pour 24000 payé par "CARTE"
    Alors la voiture "Civic 2022" a le statut "VENDUE"
    Et le total payé pour la vente de "Civic 2022" est 0
    Et le reste à payer pour la vente de "Civic 2022" est 24000

  @RF-09
  Scénario: La création d'une vente enregistre deux mouvements de type VENTE
    # Documente une redondance actuelle du code : VenteServiceImpl.creerVente() enregistre
    # explicitement un mouvement VENTE en plus de celui déjà généré par
    # VoitureService.changerStatut(VENDUE) qu'elle appelle en interne.
    Quand "Diane" vend la voiture "Civic 2022" au client "Marc Client" pour 24000 payé par "CARTE"
    Alors l'historique de la voiture "Civic 2022" contient 2 mouvements de type "VENTE"

  @RF-10
  Scénario: Un vendeur ne peut pas vendre une voiture dont il n'est pas propriétaire
    Étant donné un vendeur "Eric" qui n'est pas propriétaire de "Civic 2022"
    Quand "Eric" tente de vendre la voiture "Civic 2022" au client "Marc Client" pour 24000 payé par "CARTE"
    Alors la vente échoue avec le message "Vous n’êtes pas propriétaire"
    Et la voiture "Civic 2022" a toujours le statut "EN_STOCK"

  @RF-11
  Scénario: Chaque paiement enregistré augmente le total payé et réduit le reste à payer
    Quand "Diane" vend la voiture "Civic 2022" au client "Marc Client" pour 24000 payé par "CARTE"
    Et un paiement de 10000 par "CASH" est ajouté à la vente de "Civic 2022"
    Et un paiement de 5000 par "VIREMENT" est ajouté à la vente de "Civic 2022"
    Alors le total payé pour la vente de "Civic 2022" est 15000
    Et le reste à payer pour la vente de "Civic 2022" est 9000

  @RF-12
  Scénario: Un paiement avec une méthode inconnue est rejeté
    Quand "Diane" vend la voiture "Civic 2022" au client "Marc Client" pour 24000 payé par "CARTE"
    Et une tentative de paiement de 5000 par "BITCOIN" est faite sur la vente de "Civic 2022"
    Alors le paiement échoue avec une erreur "Methode de paiement inconnue"

  @RF-13
  Scénario: Le système ne bloque pas un paiement supérieur au reste à payer
    # Documente l'absence actuelle de contrôle empêchant un paiement > prix final :
    # PaiementServiceImpl.ajouterPaiement() n'a aucune vérification par rapport au reste à payer.
    Quand "Diane" vend la voiture "Civic 2022" au client "Marc Client" pour 24000 payé par "CARTE"
    Et un paiement de 30000 par "CASH" est ajouté à la vente de "Civic 2022"
    Alors le total payé pour la vente de "Civic 2022" est 30000
    Et le reste à payer pour la vente de "Civic 2022" est -6000
