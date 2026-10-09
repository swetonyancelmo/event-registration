package com.swetonyancelmo.event_registration.dtos;

import java.util.UUID;

public record PublishEventRequestDTO(
        UUID eventId,
        UUID organizerId
) {
}
