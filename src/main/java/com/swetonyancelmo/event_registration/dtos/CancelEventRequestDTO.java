package com.swetonyancelmo.event_registration.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Motivo do cancelamento")
public record CancelEventRequestDTO(
        @Schema(description = "Motivo do cancelamento", example = "Palestrante indisponível")
        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 255, message = "Reason must have at most 255 characters")
        String reason
) {
}
