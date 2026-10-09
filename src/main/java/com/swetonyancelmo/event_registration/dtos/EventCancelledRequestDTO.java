package com.swetonyancelmo.event_registration.dtos;

import java.util.UUID;

public record EventCancelledRequestDTO(
        UUID eventId,
        String reason,
        UUID organizerId
) {
}
