package org.autostock.services;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DempotencyServiceImp implements IdempotencyService {
    private final JdbcTemplate jdbc;

    public boolean alreadyProcessed(String consumer, String eventId) {
        Integer n = jdbc.queryForObject(
                "select count(*) from kafka_processed_event where consumer_name=? and event_id=?",
                Integer.class, consumer, eventId
        );
        return n != null && n > 0;
    }

    public void markProcessed(String consumer, String eventId) {
        jdbc.update(
                "insert into kafka_processed_event(consumer_name,event_id,processed_at) values(?,?,?)",
                consumer, eventId, LocalDateTime.now()
        );
    }
}
