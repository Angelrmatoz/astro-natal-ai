# AGENTS.md — Astro Natal AI

Contrato operativo para agentes que trabajen en este repositorio.

## Proyecto

Aplicación full-stack de astrología personalizada:

- Backend: Spring Boot + PostgreSQL.
- Frontend: SPA Vite + React + TypeScript.
- Motor astrológico: `astro-engine` (sidecar Node con `astronomy-engine`).
- IA: Gemma 4 con fallback.

El backend se construye primero; el frontend consume contratos estables.

## Estado

- Implementado: autenticación JWT, carta natal, tránsitos, lecturas IA, CORS, health del engine, `astro-engine` y persistencia.
- Frontend: fundaciones SPA (rutas, guards, cliente HTTP). Faltan onboarding, carta, tránsitos y lecturas.
- Pendiente: F2–F7 del frontend y despliegue.

## Estructura

```text
astro-natal-ai/
├── backend/      Spring Boot, PostgreSQL, tests
├── astro-engine/ cálculo astrológico (Node, puerto 3001)
├── frontend/     Vite + React + TypeScript
├── .github/      CI de GitHub Actions
├── .run/         Run configuration compartida de IntelliJ
├── README.md
└── AGENTS.md
```

## Comandos

Windows / PowerShell:

```powershell
# Base de datos
docker compose -f backend/docker-compose.db.yml up -d

# Backend con secretos de Infisical
backend\run-dev.cmd

# Backend con Maven
cd backend
infisical run --env=dev -- .\mvnw.cmd spring-boot:run

# Tests del backend
cd backend
.\mvnw.cmd test

# Frontend
cd frontend
pnpm install
pnpm dev
pnpm build
pnpm test
pnpm test:e2e

# Motor astrológico
cd astro-engine
pnpm install
pnpm test
pnpm start
```

El proyecto usa `pnpm` en todos los paquetes Node. No usar `npm` para instalar o ejecutar scripts.

## Motor astrológico

- `astro-engine/` es un servidor Node sin framework (`node:http`), puerto `3001`.
- Librería: `astronomy-engine` (MIT). No usar Swiss Ephemeris: licencia AGPL.
- Contrato en `astro-engine/README.md`: `GET /health`, `POST /natal-chart`, `POST /transits`.
- Casas de signo completo; sin hora de nacimiento no devuelve ascendente ni casas.
- El golden test compara contra fixtures de JPL Horizons con tolerancia de `0.01°` (`astro-engine/test/fixtures/`).
- Antes de cerrar un cambio del motor: `cd astro-engine && pnpm test`.
- El backend no calcula posiciones: solo orquesta y persiste el JSON del motor.

## Arquitectura del backend

```text
controller → service → repository → entity/PostgreSQL
                │
                ├─ client  (Open-Meteo, astro-engine)
                └─ dto
```

- `controller/`: HTTP, validación de entrada, autenticación.
- `service/`: reglas de negocio y orquestación.
- `repository/`: Spring Data JPA.
- `entity/`: mapeo de tablas.
- `dto/`: requests, responses y comandos internos.
- `client/`: integraciones HTTP externas.
- `config/`, `security/`, `exception/`: infraestructura transversal.

No poner lógica de negocio en controllers ni acceso a datos en services.

## Configuración y secretos

- `backend/src/main/resources/application.properties` es el archivo central.
- `application-dev.properties` y `application-prod.properties` solo contienen overrides; no hardcodear valores.
- `server.port=8080` está fijado en el central.
- Los valores vienen de `.env`, Infisical o variables de entorno de IntelliJ.
- Variables: ver `backend/.env.example`.
- `JWT_SECRET` es obligatorio y debe tener al menos 32 bytes.
- Spring no lee `.env` automáticamente; Infisical inyecta variables de entorno.
- No commitear `.env`, `.infisical.yaml` ni secretos reales.
- No agregar secretos a variables `VITE_*`; terminan en el bundle público.

## Tests y CI

- Antes de cerrar un cambio de backend: `cd backend && .\mvnw.cmd test`.
- Tests de integración usan Testcontainers; Docker Desktop debe estar corriendo.
- Los tests usan el perfil `dev` y un secreto exclusivo de test.
- No mockear PostgreSQL en tests de integración; usar Testcontainers.
- Antes de cerrar un cambio de frontend: `cd frontend && pnpm test && pnpm test:e2e`.
- Playwright usa Chromium y WebKit; instalar con `pnpm exec playwright install chromium webkit`.
- CI: `.github/workflows/backend-ci.yml`, job `backend-tests`.
- El workflow corre en PRs hacia `main`; no corre al hacer merge.
- `main` requiere PR y el check `backend-tests`; reviews requeridos: `0`.
- No hacer force-push ni borrar `main`.
- Rama de trabajo: `dev`.

## Convenciones

- Mantener el estilo existente y la arquitectura por capas.
- Cambios pequeños; no agregar dependencias sin necesidad.
- Errores HTTP con `ProblemDetail` (`application/problem+json`).
- DTOs como `record`; entidades JPA sin setters públicos innecesarios.
- No exponer entidades directamente en controllers.
- No imprimir secretos en logs, tests ni respuestas.
- Documentación humana en los `README.md`; este archivo es para agentes.

## Pendientes

1. Frontend F4–F7: tránsitos, lecturas periódicas, UX y despliegue.
2. Despliegue: Dockerfiles de backend, motor y frontend; workflow de deploy.
3. Prueba end-to-end real: backend + motor + PostgreSQL + Gemma en el mismo flujo.

No implementar funcionalidad del frontend antes de estabilizar los contratos del backend.
