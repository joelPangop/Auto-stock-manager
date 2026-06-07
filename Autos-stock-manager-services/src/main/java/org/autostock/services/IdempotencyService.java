package org.autostock.services;

public interface IdempotencyService {
    boolean alreadyProcessed(String consumer, String eventId);
    void markProcessed(String consumer, String eventId);
}
