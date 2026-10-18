package com.swetonyancelmo.event_registration.controller;

import com.swetonyancelmo.event_registration.config.OpenApiConfig;
import com.swetonyancelmo.event_registration.dtos.LoginRequestDTO;
import com.swetonyancelmo.event_registration.dtos.LoginResponseDTO;
import com.swetonyancelmo.event_registration.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.TAG_AUTH)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica o usuário",
            description = "Valida e-mail e senha e retorna um token JWT para ser enviado no header `Authorization: Bearer <token>`.")
    @ApiResponse(responseCode = "200", description = "Login realizado")
    @ApiResponse(responseCode = "400", ref = OpenApiConfig.BAD_REQUEST)
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
            content = @Content(mediaType = "application/problem+json", examples = @ExampleObject(value = """
                    {"type": "about:blank", "title": "Unauthorized", "status": 401, "detail": "Invalid credentials"}""")))
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO request) {
        return authService.login(request);
    }

}
