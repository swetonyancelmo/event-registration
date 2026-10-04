package com.swetonyancelmo.event_registration.dtos;

public record ErrorResponseDTO(
        String in,
        int status,
        String message
) {
}
