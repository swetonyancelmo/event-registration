package com.swetonyancelmo.event_registration.dtos;

import com.swetonyancelmo.event_registration.model.Event;
import com.swetonyancelmo.event_registration.model.enums.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Evento")
public record EventResponseDTO(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,
        @Schema(example = "Workshop de Spring Boot")
        String title,
        @Schema(example = "Hands-on de APIs REST com Spring Boot 4")
        String description,
        @Schema(example = "2026-12-10T19:00:00-03:00")
        OffsetDateTime startsAt,
        @Schema(example = "2026-12-10T22:00:00-03:00")
        OffsetDateTime endsAt,
        @Schema(example = "Auditório Central, São Paulo")
        String location,
        @Schema(example = "100")
        Integer capacity,
        @Schema(description = "Status atual do evento", example = "PUBLISHED")
        EventStatus status,
        @Schema(description = "Motivo do cancelamento (preenchido apenas quando `CANCELLED`)")
        String cancellationReason,
        @Schema(description = "Nome do organizador", example = "Admin")
        String organizerName
) {
    public static EventResponseDTO from(Event event) {
        return new EventResponseDTO(
                event.getId(), event.getTitle(),
                event.getDescription(), event.getStartsAt(),
                event.getEndsAt(), event.getLocation(),
                event.getCapacity(), event.getStatus(),
                event.getCancellationReason(),
                event.getOrganizer().getName()
        );
    }
}
