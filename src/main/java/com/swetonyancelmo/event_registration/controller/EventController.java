package com.swetonyancelmo.event_registration.controller;

import com.swetonyancelmo.event_registration.config.OpenApiConfig;
import com.swetonyancelmo.event_registration.dtos.CancelEventRequestDTO;
import com.swetonyancelmo.event_registration.dtos.CreateEventDTO;
import com.swetonyancelmo.event_registration.dtos.EventResponseDTO;
import com.swetonyancelmo.event_registration.model.enums.EventStatus;
import com.swetonyancelmo.event_registration.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.TAG_EVENTS)
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um evento",
            description = "Requer role `ADMIN`. O evento é criado em `DRAFT` e o usuário autenticado vira o organizador.")
    @ApiResponse(responseCode = "201", description = "Evento criado; o header `Location` aponta para o recurso")
    @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = OpenApiConfig.FORBIDDEN)
    public ResponseEntity<EventResponseDTO> create(@RequestBody @Valid CreateEventDTO request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        EventResponseDTO response = eventService.create(request, userId(jwt));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista eventos por status",
            description = """
                    Endpoint público, paginado. O token é opcional: com status `DRAFT` são retornados apenas \
                    os rascunhos organizados pelo usuário autenticado (lista vazia para anônimos).""")
    @Parameter(name = "sort", in = ParameterIn.QUERY,
            description = "Ordenação no formato `campo,(asc|desc)`. Campos permitidos: "
                    + "`startsAt`, `endsAt`, `title`, `capacity`, `createdAt`. Padrão: `startsAt,asc`.")
    @ApiResponse(responseCode = "200", description = "Página de eventos")
    @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
    public PagedModel<EventResponseDTO> list(@Parameter(description = "Status dos eventos") @RequestParam(defaultValue = "PUBLISHED") EventStatus status,
                                             @ParameterObject @PageableDefault(size = 20, sort = "startsAt", direction = Sort.Direction.ASC) Pageable pageable,
                                             @AuthenticationPrincipal Jwt jwt) {
        return new PagedModel<>(eventService.list(status, userIdOrNull(jwt), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um evento pelo ID",
            description = "Endpoint público. Rascunhos (`DRAFT`) só são retornados para o organizador; para os demais, 404.")
    @ApiResponse(responseCode = "200", description = "Evento encontrado")
    @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
    public EventResponseDTO findById(@Parameter(description = "ID do evento") @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return eventService.findById(id, userIdOrNull(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um evento",
            description = "Apenas o organizador pode editar, e somente enquanto o evento estiver em `DRAFT`.")
    @ApiResponse(responseCode = "200", description = "Evento atualizado")
    @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = OpenApiConfig.FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = OpenApiConfig.CONFLICT)
    public EventResponseDTO update(@Parameter(description = "ID do evento") @PathVariable UUID id,
                                   @RequestBody @Valid CreateEventDTO request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return eventService.update(id, request, userId(jwt));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Publica um evento", description = "Transição `DRAFT → PUBLISHED`. Apenas o organizador.")
    @ApiResponse(responseCode = "200", description = "Estado do evento atualizado")
    @ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = OpenApiConfig.FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = OpenApiConfig.CONFLICT)
    public EventResponseDTO publish(@Parameter(description = "ID do evento") @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return eventService.publish(id, userId(jwt));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancela um evento",
            description = "Transição `DRAFT | PUBLISHED → CANCELLED`, com motivo obrigatório. Apenas o organizador.")
    @ApiResponse(responseCode = "200", description = "Estado do evento atualizado")
    @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = OpenApiConfig.FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = OpenApiConfig.CONFLICT)
    public EventResponseDTO cancel(@Parameter(description = "ID do evento") @PathVariable UUID id,
                                   @RequestBody @Valid CancelEventRequestDTO request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return eventService.cancel(id, request.reason(), userId(jwt));
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Finaliza um evento", description = "Transição `PUBLISHED → FINISHED`. Apenas o organizador.")
    @ApiResponse(responseCode = "200", description = "Estado do evento atualizado")
    @ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = OpenApiConfig.FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
    @ApiResponse(responseCode = "409", ref = OpenApiConfig.CONFLICT)
    public EventResponseDTO finish(@Parameter(description = "ID do evento") @PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return eventService.finish(id, userId(jwt));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static UUID userIdOrNull(Jwt jwt) {
        return jwt == null ? null : userId(jwt);
    }

}
