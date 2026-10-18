package com.swetonyancelmo.event_registration.controller;

import com.swetonyancelmo.event_registration.config.OpenApiConfig;
import com.swetonyancelmo.event_registration.dtos.CreateUserDTO;
import com.swetonyancelmo.event_registration.dtos.UpdateUserRoleDTO;
import com.swetonyancelmo.event_registration.dtos.UserResponseDTO;
import com.swetonyancelmo.event_registration.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.TAG_USERS)
public class UserController {

    private final UserService userService;

    @PostMapping
    @SecurityRequirements
    @Operation(summary = "Cadastra um usuário",
            description = "Endpoint público. O usuário é criado com a role `USER`; o e-mail é normalizado para minúsculas.")
    @ApiResponse(responseCode = "201", description = "Usuário criado; o header `Location` aponta para o recurso")
    @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
            content = @Content(mediaType = "application/problem+json", examples = @ExampleObject(value = """
                    {"type": "about:blank", "title": "Email already registered", "status": 409, "detail": "Email already registered"}""")))
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid CreateUserDTO request) {
        UserResponseDTO response = userService.register(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna o usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Dados do usuário logado")
    @ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
    public UserResponseDTO me(@AuthenticationPrincipal Jwt jwt) {
        return userService.findById(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Operation(summary = "Busca um usuário pelo ID", description = "Permitido para `ADMIN` ou para o próprio usuário.")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado")
    @ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = OpenApiConfig.FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
    public UserResponseDTO findById(@Parameter(description = "ID do usuário") @PathVariable UUID id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Altera a role de um usuário", description = "Requer role `ADMIN`.")
    @ApiResponse(responseCode = "200", description = "Role atualizada")
    @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
    @ApiResponse(responseCode = "401", ref = OpenApiConfig.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", ref = OpenApiConfig.FORBIDDEN)
    @ApiResponse(responseCode = "404", ref = OpenApiConfig.NOT_FOUND)
    public UserResponseDTO updateRole(@Parameter(description = "ID do usuário") @PathVariable UUID id,
                                      @RequestBody @Valid UpdateUserRoleDTO request) {
        return userService.updateRole(id, request.role());
    }

}
