# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
./mvnw spring-boot:run          # Run the application (port 8080 by default)
./mvnw clean package            # Build executable JAR
./mvnw test                     # Run all tests
./mvnw test -Dtest=ClassName    # Run a specific test class
```

The `PORT` environment variable overrides the default port: `PORT=9090 ./mvnw spring-boot:run`.

## Architecture

Spring Boot 4.0.4 REST API — no service layer. Controllers call repositories directly.

```
controller/  →  repository/  →  PostgreSQL (Neon cloud)
```

**Controllers:**
- `UsuarioController` (`/api/usuarios`) — register, login, get profile, update profile
- `ChatController` (`/api/chat`) — room info, list messages, send message, verify session

**Models (JPA entities):**
- `Usuario` — users table, passwords hashed with jBCrypt
- `Sala` — chat rooms keyed by a string ID (e.g. `"GENERAL"`); created manually or via QR
- `MensajeGrupal` — messages stored in `log_mensajes_grupal`

**Repositories:** Spring Data JPA. `MensajeRepository` has one custom JPQL query (`obtenerMensajesPorSala`).

## Database

PostgreSQL hosted on Neon (AWS EU). Connection details are in `application.properties`. `spring.jpa.hibernate.ddl-auto=update` — schema is managed automatically from entity changes.

`Sala` rows must be seeded manually; the app validates that a room exists before accepting messages or returning room info.

## Key Design Notes

- No JWT or session tokens — login returns `id_usuario` directly; clients store it and pass it as a request param.
- User name is resolved at read time in `ChatController.getMensajesGrupal` (N+1 pattern — one DB call per message to fetch sender name).
- All request parameters are sent as form params (`@RequestParam`), not JSON body.
- Passwords are BCrypt-hashed on registration and update; raw password is never stored.
