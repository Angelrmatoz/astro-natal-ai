# Backend — Astro Natal AI

API REST en Spring Boot para autenticación, cartas natales y tránsitos.

## Stack

- Java 25
- Spring Boot 4.1
- Spring Security + JWT HS256
- Spring Data JPA + Hibernate
- PostgreSQL 18
- Flyway
- Testcontainers
- Maven Wrapper

## Arquitectura

```text
controller → service → repository → PostgreSQL
                │
                ├─ client  (Open-Meteo, astro-engine)
                └─ entity  (mapeo JPA)
```

```text
src/main/java/org/astronatalai/backend/
├── controller/   AuthController, UserController, ChartController, TransitController, ReadingController
├── service/      AuthService, UserService, ChartService, TransitService, ReadingService, ReadingType, PromptBuilder, JwtService
├── repository/   UserRepository, BirthChartRepository, ReadingRepository
├── entity/       User, BirthChart, Reading
├── dto/          Requests, responses y comandos internos
├── client/       GeocodingClient, AstroEngineClient, GemmaClient
├── config/       SecurityConfig, CorsConfig, RestClientConfig, ClockConfig, EngineHealthIndicator
├── security/     Entry point y access denied handler
├── exception/    Excepciones y ApiExceptionHandler
└── BackendApplication
```

## Configuración

`src/main/resources/application.properties` es el archivo central: datasource por variables, JPA, Flyway, puerto `8080`, JWT, geocoding, modelos de IA y timeouts. Los secretos nunca se escriben ahí.

Los perfiles solo contienen lo que cambia entre entornos:

- `application-dev.properties`: motor en `http://localhost:3001` y CORS hacia Vite (`http://localhost:5173`) como defaults; salud con detalle.
- `application-prod.properties`: `PORT` de la plataforma, URL del motor y orígenes CORS obligatorios; salud sin detalle.

Los valores reales vienen de:

1. `.env` local cargado en IntelliJ (ignorado por git), o
2. Infisical, o
3. variables de entorno de IntelliJ.

### Variables requeridas

| Variable | Descripción |
|---|---|
| `DB_HOST` | Host de PostgreSQL |
| `DB_PORT` | Puerto de PostgreSQL |
| `POSTGRES_DB` | Base de datos |
| `POSTGRES_USER` | Usuario |
| `POSTGRES_PASSWORD` | Contraseña |
| `JWT_SECRET` | Secreto HS256, mínimo 32 bytes |
| `JWT_TTL` | Duración, por ejemplo `PT24H` |
| `JWT_ISSUER` | Emisor del JWT |
| `GEOCODING_URL` | Base de Open-Meteo |
| `ASTRO_ENGINE_URL` | URL interna del motor |
| `GOOGLE_AI_API_KEY` | API key de Google AI Studio |
| `GOOGLE_AI_MODEL` | Modelo principal: `gemma-4-31b-it` |
| `GOOGLE_AI_FALLBACK_MODEL` | Fallback: `gemma-4-26b-a4b-it` |
| `GOOGLE_AI_TIMEOUT_SECONDS` | Timeout de la llamada a IA, en segundos (300) |
| `CORS_ALLOWED_ORIGINS` | Origen permitido del frontend |

`.env.example` contiene todas las claves con valores de ejemplo. El `.env` real no se versiona.

## Base de datos

```powershell
docker compose -f docker-compose.db.yml up -d
```

Flyway aplica las migraciones al arrancar. Para desarrollo, el perfil `dev` se activa con:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
```

## Ejecutar

Con Infisical:

```powershell
infisical run --env=dev -- .\mvnw.cmd spring-boot:run
```

O con el wrapper:

```powershell
.\run-dev.cmd
```

También puedes ejecutar `BackendApplication` desde IntelliJ con perfil `dev` y las variables de entorno configuradas.

## Tests

```powershell
.\mvnw.cmd test
```

- Unitarios: servicios y clientes.
- Integración: `@SpringBootTest` + `MockMvc` + PostgreSQL con Testcontainers.
- El contexto de integración usa el perfil `dev` y propiedades exclusivas de test.
- Docker Desktop debe estar corriendo para Testcontainers.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/auth/register` | Registro y token |
| `POST` | `/api/auth/login` | Login y token |
| `PATCH` | `/api/users/me` | Actualizar el nombre del perfil |
| `POST` | `/api/charts` | Crear o actualizar la carta |
| `GET` | `/api/charts/me` | Carta del usuario autenticado |
| `GET` | `/api/transits/current` | Tránsitos actuales |
| `POST` | `/api/readings` | Lectura IA: `day`, `week`, `month` o `natal` |
| `GET` | `/actuator/health` | Healthcheck |

Los errores usan `application/problem+json` (`ProblemDetail`).

## Smoke manual

Con el backend arriba:

```powershell
curl.exe -s http://localhost:8080/actuator/health/liveness

$body = '{"email":"smoke@example.com","password":"secret123","nombre":"Smoke"}'
$register = curl.exe -s -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d $body
$token = (ConvertFrom-Json $register).token

curl.exe -s http://localhost:8080/api/charts/me -H "Authorization: Bearer $token"
```

- Registro, login y endpoints protegidos funcionan sin `astro-engine`.
- Carta, tránsitos y lecturas responden `503` si el motor o la IA no están disponibles.
- `/actuator/health` incluye el engine; si está apagado puede dar `DOWN`. Para comprobar el proceso usa `/actuator/health/liveness`.

## Estado

- Autenticación, carta natal, tránsitos, lecturas IA, CORS y health del engine implementados.
- `astro-engine` implementado y verificado contra JPL Horizons con error menor a `0.01°`.
- Lectura integral de carta (`type: natal`) con Gemma 4; modelo principal `gemma-4-31b-it`, fallback `gemma-4-26b-a4b-it`.
- Los tests mockean motor e IA; el flujo real se valida con Postgres, motor y clave de Google arriba.
- Mientras `astro-engine` no esté arriba, `/actuator/health` puede reportar `DOWN`; `/actuator/health/liveness` sigue disponible.
