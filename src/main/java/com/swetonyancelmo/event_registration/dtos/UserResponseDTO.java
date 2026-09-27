package com.swetonyancelmo.event_registration.dtos;

import com.swetonyancelmo.event_registration.model.enums.UserRole;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        UserRole role
) {
}
