# Astro Natal AI

Aplicación full-stack de astrología personalizada: el usuario registra sus datos de nacimiento, obtiene su carta natal y recibe lecturas diarias, semanales y mensuales cruzando la carta con los tránsitos actuales.

El proyecto se construye primero el backend, con contratos estables y tests, y después el frontend.

## Estado

- Backend: autenticación JWT, carta natal, tránsitos, lecturas IA (diaria, semanal, mensual e integral de carta) y persistencia en PostgreSQL.
- `astro-engine`: sidecar de cálculo astrológico con `astronomy-engine`; posiciones verificadas contra JPL Horizons con error menor a `0.01°`.
- Frontend: fundaciones SPA, registro con datos de nacimiento, perfil editable, carta natal, tránsitos y lecturas.
- Pendiente: despliegue (F7).

## Stack

- Backend: Java 25, Spring Boot 4.1, Spring Security, JWT HS256, JPA, Flyway, PostgreSQL 18.
- Motor: Node.js 24 con `astronomy-engine` (MIT).
- Frontend: Vite 8, React 19, TypeScript 7, React Router, pnpm.
- Infraestructura: Docker Compose para PostgreSQL, Testcontainers para integración.
- Secretos: Infisical.
- IA: Gemma 4 vía Google GenAI SDK.

## Estructura

```text
astro-natal-ai/
├── backend/          Spring Boot + PostgreSQL + tests
├── astro-engine/     sidecar Node de cálculo astrológico
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
| `GOOGLE_AI_API_KEY` | API key de Google AI Studio |
| `GOOGLE_AI_MODEL` | Modelo principal, `gemma-4-31b-it` |
| `GOOGLE_AI_FALLBACK_MODEL` | Fallback, `gemma-4-26b-a4b-it` |
| `GOOGLE_AI_TIMEOUT_SECONDS` | Timeout de la llamada a IA, en segundos |
| `CORS_ALLOWED_ORIGINS` | Origen permitido del frontend, separado por comas |

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

2. Motor astrológico:

```powershell
cd astro-engine
pnpm install
pnpm start
```

3. Backend:

```powershell
cd backend
infisical run --env=dev -- .\mvnw.cmd spring-boot:run
```

4. Frontend:

```powershell
cd frontend
pnpm install
pnpm dev
```

Backend: `http://localhost:8080` · Motor: `http://localhost:3001` · Frontend: `http://localhost:5173`

## Tests

```powershell
cd backend
.\mvnw.cmd test

cd astro-engine
pnpm test

cd frontend
pnpm test
pnpm exec playwright install chromium webkit   # una vez
pnpm test:e2e
```

El backend usa JUnit 5, Mockito y Testcontainers. El motor valida sus posiciones contra fixtures de JPL Horizons con tolerancia de `0.01°`. El frontend usa Jest y React Testing Library para componentes, y Playwright con Chromium y WebKit para flujos.

## API

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/auth/register` | Registro |
| `POST` | `/api/auth/login` | Login y JWT |
| `PATCH` | `/api/users/me` | Actualizar el nombre del perfil |
| `POST` | `/api/charts` | Crear/actualizar carta natal |
| `GET` | `/api/charts/me` | Carta del usuario |
| `GET` | `/api/transits/current` | Tránsitos actuales |
| `POST` | `/api/readings` | Lectura IA: `day`, `week`, `month` o `natal` |
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
- El `astro-engine` calcula la carta y los tránsitos; el backend no inventa posiciones.
