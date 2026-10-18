# event-registration

API REST para cadastro e gestão de eventos.

**Stack:** Java 25 · Spring Boot 4 · Spring Security (JWT) · PostgreSQL · Flyway · Testcontainers · springdoc-openapi

## Como rodar

### Banco no Docker + aplicação local (profile `dev`)

```bash
docker compose up -d database
mvn spring-boot:run
```

O profile `dev` é o padrão e já traz valores locais (usuário `postgres/postgres`, segredo JWT de desenvolvimento)
e cria o admin inicial `admin@local.dev` / `admin12345`.

### Tudo no Docker

```bash
docker compose --profile app up --build
```

### Produção (profile `prod`)

Defina `SPRING_PROFILES_ACTIVE=prod` e as variáveis:

| Variável | Descrição |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Conexão com o PostgreSQL |
| `DB_USER`, `DB_PASSWORD` | Credenciais do banco (obrigatórias) |
| `JWT_SECRET` | Segredo HS256 com no mínimo 32 bytes (obrigatório) |
| `JWT_EXPIRATION` | Validade do token (padrão `1h`) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` | Admin inicial, criado só se ainda não existir |

## Testes

```bash
mvn verify
```

Os testes de integração sobem um PostgreSQL com Testcontainers, então é preciso ter o Docker rodando.

## Documentação

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/actuator/health

Erros seguem o formato [RFC 9457 (Problem Details)](https://www.rfc-editor.org/rfc/rfc9457).

## Endpoints

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/v1/users` | público | Cadastro de usuário (role `USER`) |
| POST | `/api/v1/auth/login` | público | Retorna o JWT |
| GET | `/api/v1/users/me` | autenticado | Dados do usuário logado |
| GET | `/api/v1/users/{id}` | admin ou o próprio | Busca usuário |
| PATCH | `/api/v1/users/{id}/role` | admin | Altera a role de um usuário |
| GET | `/api/v1/events?status=PUBLISHED&page=0&size=20` | público | Lista paginada (rascunhos só para o organizador) |
| GET | `/api/v1/events/{id}` | público | Detalhe do evento |
| POST | `/api/v1/events` | admin | Cria evento em `DRAFT` |
| PUT | `/api/v1/events/{id}` | organizador | Edita evento (só em `DRAFT`) |
| POST | `/api/v1/events/{id}/publish` | organizador | Publica |
| POST | `/api/v1/events/{id}/cancel` | organizador | Cancela (corpo `{"reason": "..."}`) |
| POST | `/api/v1/events/{id}/finish` | organizador | Finaliza |

Envie o token no header `Authorization: Bearer <token>`.

## Máquina de estados do evento

```
DRAFT ──publish──> PUBLISHED ──finish──> FINISHED
  │                    │
  └──────cancel────────┴──> CANCELLED
```

- Só eventos `DRAFT` podem ser editados ou publicados.
- Só eventos `PUBLISHED` podem ser finalizados.
- Eventos `FINISHED` ou `CANCELLED` não podem ser cancelados.
- Transições inválidas retornam **409 Conflict**.
