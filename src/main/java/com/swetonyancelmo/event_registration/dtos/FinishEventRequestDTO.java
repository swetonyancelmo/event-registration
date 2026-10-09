package com.swetonyancelmo.event_registration.dtos;

import java.util.UUID;

public record FinishEventRequestDTO(
        UUID eventId,
        UUID organizerId
) {
}
