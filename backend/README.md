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
├── controller/   AuthController, ChartController, TransitController
├── service/      AuthService, ChartService, TransitService, ReadingService, PromptBuilder, JwtService
├── repository/   UserRepository, BirthChartRepository, ReadingRepository
├── entity/       User, BirthChart, Reading
├── dto/          Requests, responses y comandos internos
├── client/       GeocodingClient, AstroEngineClient, GemmaClient
├── config/       SecurityConfig, RestClientConfig, ClockConfig
├── security/     Entry point y access denied handler
├── exception/    Excepciones y ApiExceptionHandler
└── BackendApplication
```

## Configuración

`src/main/resources/application.properties` es el archivo central. No contiene secretos ni valores de entorno; solo placeholders.

Los perfiles `application-dev.properties` y `application-prod.properties` solo contienen overrides. Los valores reales vienen de:

1. `.env` local (ignorado por git), o
2. Infisical, o
3. variables de entorno de IntelliJ.

`server.port=8080` está fijado en el central.

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
| `GOOGLE_AI_TIMEOUT_SECONDS` | Timeout de la llamada a IA |
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
| `POST` | `/api/charts` | Crear o actualizar la carta |
| `GET` | `/api/charts/me` | Carta del usuario autenticado |
| `GET` | `/api/transits/current` | Tránsitos actuales |
| `POST` | `/api/readings` | Lectura IA diaria/semanal/mensual |
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
- Carta, tránsitos y lecturas devuelven `503` hasta que el engine esté disponible.
- `/actuator/health` incluye el engine; si está apagado puede dar `DOWN`. Para comprobar el proceso usa `/actuator/health/liveness`.

## Estado

- Autenticación, carta natal, tránsitos, lecturas IA, CORS y health del engine implementados.
- Endurecimiento completado.
- `astro-engine` pendiente: los endpoints que dependen de él devuelven `503` hasta que exista.
- Las cartas y los tránsitos se prueban contra fixtures; falta el golden test del motor real.
- Las lecturas usan Gemma 4 con modelo principal `gemma-4-31b-it` y fallback `gemma-4-26b-a4b-it`; los tests mockean la IA.
- Mientras `astro-engine` no esté arriba, `/actuator/health` puede reportar `DOWN`; `/actuator/health/liveness` sigue disponible.
