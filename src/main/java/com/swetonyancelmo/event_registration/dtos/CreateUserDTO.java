package com.swetonyancelmo.event_registration.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de usuário")
public record CreateUserDTO(
        @Schema(description = "Nome completo", example = "Maria Silva")
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must have at most 255 characters")
        String name,

        @Schema(description = "E-mail (usado no login)", example = "maria@email.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email has an invalid format")
        @Size(max = 255, message = "Email must have at most 255 characters")
        String email,

        @Schema(description = "Senha", example = "senhaSegura123")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must have between 8 and 72 characters")
        String password
) {
}
