package com.swetonyancelmo.event_registration.dtos;

import com.swetonyancelmo.event_registration.model.User;
import com.swetonyancelmo.event_registration.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados públicos do usuário")
public record UserResponseDTO(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(example = "Maria Silva")
        String name,
        @Schema(example = "maria@email.com")
        String email,
        @Schema(example = "USER")
        UserRole role
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
