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
├── service/      AuthService, ChartService, TransitService, JwtService
├── repository/   UserRepository, BirthChartRepository
├── entity/       User, BirthChart
├── dto/          Requests, responses y comandos internos
├── client/       GeocodingClient, AstroEngineClient
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
| `GET` | `/actuator/health` | Healthcheck |

Los errores usan `application/problem+json` (`ProblemDetail`).

## Estado

- Autenticación, carta natal y tránsitos implementados.
- Lecturas IA y endurecimiento pendientes.
- `astro-engine` pendiente: los endpoints que dependen de él devuelven `503` hasta que exista.
- Las cartas y los tránsitos se prueban contra fixtures; falta el golden test del motor real.
