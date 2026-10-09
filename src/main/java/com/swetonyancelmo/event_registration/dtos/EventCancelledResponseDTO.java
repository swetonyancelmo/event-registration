package com.swetonyancelmo.event_registration.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventCancelledResponseDTO(
        UUID eventId,
        String title,
        LocalDateTime startsAt,
        String organizerName,
        String reason
) {
}
