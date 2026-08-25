-- Table technique utilisée par DempotencyServiceImp (JdbcTemplate en SQL brut, pas une @Entity JPA).
-- Elle n'existe dans aucune migration du dépôt : en production elle est vraisemblablement créée
-- manuellement. On la recrée ici pour permettre aux tests H2 de fonctionner.
-- NB : le code applicatif ne pose pas de contrainte d'unicité sur (consumer_name, event_id) avant
-- l'insertion — voir idempotency.feature pour le scénario qui documente ce comportement.
CREATE TABLE IF NOT EXISTS kafka_processed_event (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumer_name  VARCHAR(255) NOT NULL,
    event_id       VARCHAR(255) NOT NULL,
    processed_at   TIMESTAMP    NOT NULL
);
