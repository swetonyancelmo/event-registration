package com.swetonyancelmo.event_registration.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Schema(description = "Dados para criar ou editar um evento")
public record CreateEventDTO(
        @Schema(description = "Título do evento", example = "Workshop de Spring Boot")
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must have at most 255 characters")
        String title,

        @Schema(description = "Descrição do evento", example = "Hands-on de APIs REST com Spring Boot 4")
        @NotBlank(message = "Description is required")
        @Size(max = 255, message = "Description must have at most 255 characters")
        String description,

        @Schema(description = "Início do evento (ISO-8601 com offset, deve estar no futuro)", example = "2026-12-10T19:00:00-03:00")
        @NotNull(message = "Start date is required")
        @Future(message = "Start date must be in the future")
        OffsetDateTime startsAt,

        @Schema(description = "Término do evento (deve ser posterior ao início)", example = "2026-12-10T22:00:00-03:00")
        @NotNull(message = "End date is required")
        OffsetDateTime endsAt,

        @Schema(description = "Local do evento", example = "Auditório Central, São Paulo")
        @NotBlank(message = "Location is required")
        @Size(max = 255, message = "Location must have at most 255 characters")
        String location,

        @Schema(description = "Número máximo de participantes", example = "100")
        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be greater than zero")
        Integer capacity
) {
    @JsonIgnore
    @AssertTrue(message = "End date must be after start date")
    public boolean isDateRangeValid() {
        return startsAt == null || endsAt == null || endsAt.isAfter(startsAt);
    }
}
