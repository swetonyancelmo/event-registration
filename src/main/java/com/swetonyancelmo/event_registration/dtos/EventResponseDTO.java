package com.swetonyancelmo.event_registration.dtos;

import com.swetonyancelmo.event_registration.model.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponseDTO(
        UUID id,
        String title,
        String description,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String location,
        Integer capacity,
        EventStatus status,
        String organizerName
) {
}
