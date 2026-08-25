# language: fr
# Règles métier couvertes (déduites de IdempotencyService / DempotencyServiceImp) :
#  - Un événement (consumer, eventId) n'est "déjà traité" que s'il a été marqué au préalable.
#  - Le marquage est spécifique à la paire (consumer, eventId) : changer l'un des deux
#    identifiants correspond à un événement distinct.
@idempotency
Fonctionnalité: Idempotence du traitement des événements (consommateurs)

  @RF-14
  Scénario: Un événement jamais marqué n'est pas considéré comme traité
    Alors l'événement "evt-1" du consommateur "paiement-consumer" n'est pas déjà traité

  @RF-15
  Scénario: Marquer un événement comme traité empêche son retraitement
    Quand l'événement "evt-1" du consommateur "paiement-consumer" est marqué comme traité
    Alors l'événement "evt-1" du consommateur "paiement-consumer" est déjà traité

  @RF-16
  Scénario: Le même identifiant d'événement pour un autre consommateur n'est pas affecté
    Quand l'événement "evt-1" du consommateur "paiement-consumer" est marqué comme traité
    Alors l'événement "evt-1" du consommateur "stock-consumer" n'est pas déjà traité

  @RF-16
  Scénario: Un autre identifiant d'événement pour le même consommateur n'est pas affecté
    Quand l'événement "evt-1" du consommateur "paiement-consumer" est marqué comme traité
    Alors l'événement "evt-2" du consommateur "paiement-consumer" n'est pas déjà traité

  @RF-17
  Scénario: Marquer deux fois le même événement ne provoque pas d'erreur
    # Documente l'absence de contrainte d'unicité en base sur (consumer_name, event_id) :
    # markProcessed() peut être appelé plusieurs fois pour le même événement sans exception.
    Quand l'événement "evt-1" du consommateur "paiement-consumer" est marqué comme traité
    Et l'événement "evt-1" du consommateur "paiement-consumer" est marqué comme traité
    Alors l'événement "evt-1" du consommateur "paiement-consumer" est déjà traité
