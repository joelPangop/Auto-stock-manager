package org.autostock.services;

import org.autostock.dtos.client.ReservationCreateDto;
import org.autostock.dtos.client.ReservationDto;

import java.util.List;

public interface ReservationService {
    ReservationDto create(String clientEmail, ReservationCreateDto dto);
    List<ReservationDto> listByClient(String clientEmail);
    void cancel(String clientEmail, Long reservationId);
}
