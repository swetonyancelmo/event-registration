package com.swetonyancelmo.event_registration.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login")
public record LoginRequestDTO(
        @Schema(description = "E-mail cadastrado", example = "admin@local.dev")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "Senha", example = "admin12345")
        @NotBlank(message = "Password is required")
        String password
) {
}
