# Astro Natal AI

Aplicación full-stack de astrología personalizada: el usuario registra sus datos de nacimiento, obtiene su carta natal y recibe lecturas diarias, semanales y mensuales cruzando la carta con los tránsitos actuales.

El proyecto se construye primero el backend, con contratos estables y tests, y después el frontend.

## Estado

- Backend: autenticación JWT, carta natal, tránsitos y persistencia en PostgreSQL.
- Frontend: scaffold de Vite + React + TypeScript.
- Pendiente: lecturas con Gemma 4, endurecimiento, UX del frontend y despliegue.
- `astro-engine` todavía no existe; los endpoints de carta y tránsitos responden `503` hasta que esté disponible.

## Stack

- Backend: Java 25, Spring Boot 4.1, Spring Security, JWT HS256, JPA, Flyway, PostgreSQL 18.
- Frontend: Vite 8, React 19, TypeScript 7, React Router, pnpm.
- Infraestructura: Docker Compose para PostgreSQL, Testcontainers para integración.
- Secretos: Infisical.
- IA prevista: Gemma 4 vía Google GenAI SDK.

## Estructura

```text
astro-natal-ai/
├── backend/          Spring Boot + PostgreSQL + tests
├── frontend/         SPA Vite + React
├── .github/          CI de GitHub Actions
├── .run/             Run configuration compartida de IntelliJ
└── README.md
```

## Requisitos

- Java 25
- Docker Desktop
- Node.js 24+ y pnpm
- Infisical CLI (recomendado para desarrollo)
- IntelliJ IDEA (opcional)

## Configuración

No hay secretos en el repositorio. Cada app tiene su propio `.env.example`:

- `backend/.env.example`
- `frontend/.env.example`

Los `.env` reales están ignorados por git.

### Variables del backend

| Variable | Uso |
|---|---|
| `DB_HOST` | Host de PostgreSQL |
| `DB_PORT` | Puerto de PostgreSQL |
| `POSTGRES_DB` | Nombre de la base |
| `POSTGRES_USER` | Usuario de la base |
| `POSTGRES_PASSWORD` | Contraseña de la base |
| `JWT_SECRET` | Firma de tokens; mínimo 32 bytes |
| `JWT_TTL` | Duración del token, por ejemplo `PT24H` |
| `JWT_ISSUER` | Emisor del token |
| `GEOCODING_URL` | Base de Open-Meteo Geocoding |
| `ASTRO_ENGINE_URL` | URL interna del motor astrológico |

`application.properties` es el archivo central. Los perfiles `dev` y `prod` solo contienen overrides; los valores vienen de `.env`, Infisical o variables de IntelliJ. `server.port=8080` está fijado en el central.

### Infisical

```powershell
infisical login
cd C:\Dev\astro-natal-ai
infisical init          # una vez por laptop; seleccionar astro-natal-ai
infisical run --env=dev -- .\mvnw.cmd spring-boot:run
```

También puedes usar el wrapper:

```powershell
backend\run-dev.cmd
```

En IntelliJ hay una Run Configuration compartida: **Backend (Infisical)**. Si usas el play de `BackendApplication`, asigna el perfil `dev` y las variables de entorno manualmente en **Edit Configurations**.

## Arranque rápido

1. Levantar PostgreSQL:

```powershell
docker compose -f backend/docker-compose.db.yml up -d
```

2. Backend:

```powershell
cd backend
infisical run --env=dev -- .\mvnw.cmd spring-boot:run
```

3. Frontend:

```powershell
cd frontend
pnpm install
pnpm dev
```

Backend: `http://localhost:8080` · Frontend: `http://localhost:5173`

## Tests

```powershell
cd backend
.\mvnw.cmd test
```

Incluye tests unitarios y de integración con Testcontainers. Docker Desktop debe estar corriendo.

## API

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/auth/register` | Registro |
| `POST` | `/api/auth/login` | Login y JWT |
| `POST` | `/api/charts` | Crear/actualizar carta natal |
| `GET` | `/api/charts/me` | Carta del usuario |
| `GET` | `/api/transits/current` | Tránsitos actuales |
| `GET` | `/actuator/health` | Healthcheck |

## CI y ramas

- `dev` es la rama de trabajo.
- `main` está protegida: requiere PR y el check `backend-tests`.
- El workflow corre en pull requests hacia `main`; no se ejecuta al hacer merge.
- Reviews requeridos: `0`.
- No se permiten force-push ni borrado de `main`.

## Notas de seguridad

- Nunca commitear `.env`, `.infisical.yaml` ni secretos reales.
- `.env.example` solo contiene nombres y valores de ejemplo.
- `JWT_SECRET` es obligatorio; si falta, el backend falla al arrancar.
- El `astro-engine` debe calcular la carta y los tránsitos; el backend no inventa posiciones.
