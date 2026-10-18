package com.swetonyancelmo.event_registration.dtos;

import com.swetonyancelmo.event_registration.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nova role do usuário")
public record UpdateUserRoleDTO(
        @Schema(description = "Nova role", example = "ADMIN")
        @NotNull(message = "Role is required")
        UserRole role
) {
}
