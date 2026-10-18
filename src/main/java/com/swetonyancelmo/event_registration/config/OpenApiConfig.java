package com.swetonyancelmo.event_registration.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    public static final String TAG_AUTH = "Autenticação";
    public static final String TAG_USERS = "Usuários";
    public static final String TAG_EVENTS = "Eventos";

    // Respostas de erro reutilizáveis, referenciadas nos controllers via @ApiResponse(ref = ...)
    public static final String BAD_REQUEST = "BadRequest";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String FORBIDDEN = "Forbidden";
    public static final String NOT_FOUND = "NotFound";
    public static final String CONFLICT = "Conflict";

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String PROBLEM_DETAIL = "ProblemDetail";

    private static final String DESCRIPTION = """
            API REST para cadastro e gestão de eventos.

            ### Autenticação
            1. Cadastre-se em `POST /api/v1/users` (ou use o admin inicial do profile `dev`: `admin@local.dev` / `admin12345`).
            2. Faça login em `POST /api/v1/auth/login` e copie o `accessToken`.
            3. Clique em **Authorize** e cole o token (sem o prefixo `Bearer`).

            ### Ciclo de vida do evento
            ```
            DRAFT ──publish──> PUBLISHED ──finish──> FINISHED
              │                    │
              └──────cancel────────┴──> CANCELLED
            ```
            - Só eventos `DRAFT` podem ser editados ou publicados.
            - Só eventos `PUBLISHED` podem ser finalizados.
            - Eventos `FINISHED` ou `CANCELLED` não podem ser cancelados.
            - Rascunhos (`DRAFT`) só são visíveis para o organizador.

            ### Erros
            Todos os erros seguem o formato [RFC 9457 (Problem Details)](https://www.rfc-editor.org/rfc/rfc9457).
            """;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Registration API")
                        .version("v1")
                        .description(DESCRIPTION))
                .tags(List.of(
                        new Tag().name(TAG_AUTH).description("Login e emissão do token JWT"),
                        new Tag().name(TAG_USERS).description("Cadastro e consulta de usuários"),
                        new Tag().name(TAG_EVENTS).description("Criação, consulta e ciclo de vida dos eventos")))
                .components(new Components()
                        .addSecuritySchemes(BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                .description("Token obtido em `POST /api/v1/auth/login`"))
                        .addSchemas(PROBLEM_DETAIL, problemDetailSchema())
                        .addResponses(BAD_REQUEST, problem("Requisição inválida (validação do corpo ou parâmetros)",
                                "validation", "Validation failed", 400, "One or more fields are invalid",
                                Map.of("title", "Title is required")))
                        .addResponses(UNAUTHORIZED, new ApiResponse()
                                .description("Token ausente, inválido ou expirado"))
                        .addResponses(FORBIDDEN, problem("Usuário sem permissão para esta operação",
                                "forbidden", "Forbidden", 403, "Access Denied", null))
                        .addResponses(NOT_FOUND, problem("Recurso não encontrado",
                                "notFound", "Resource not found", 404, "Event not found", null))
                        .addResponses(CONFLICT, problem("Transição de estado inválida ou modificação concorrente",
                                "invalidTransition", "Invalid state transition", 409,
                                "Only DRAFT events can be published", null)))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    private static Schema<?> problemDetailSchema() {
        return new ObjectSchema()
                .description("Erro no formato RFC 9457")
                .addProperty("type", new StringSchema().example("about:blank"))
                .addProperty("title", new StringSchema().example("Resource not found"))
                .addProperty("status", new IntegerSchema().example(404))
                .addProperty("detail", new StringSchema().example("Event not found"))
                .addProperty("instance", new StringSchema().example("/api/v1/events/3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .addProperty("errors", new MapSchema()
                        .additionalProperties(new StringSchema())
                        .description("Erros por campo (apenas em falhas de validação)"));
    }

    private static ApiResponse problem(String description, String exampleName, String title, int status,
                                       String detail, Map<String, String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", title);
        body.put("status", status);
        body.put("detail", detail);
        if (errors != null) {
            body.put("errors", errors);
        }
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(PROBLEM_JSON, new MediaType()
                        .schema(new Schema<>().$ref(PROBLEM_DETAIL))
                        .addExamples(exampleName, new Example().value(body))));
    }

}
