package com.swetonyancelmo.event_registration.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateEventDTO(
        String title,
        String description,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String location,
        Integer capacity,
        UUID organizer_id
) {
}
