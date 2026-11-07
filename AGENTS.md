# AGENTS.md — Astro Natal AI

Contrato operativo para agentes que trabajen en este repositorio.

## Proyecto

Aplicación full-stack de astrología personalizada:

- Backend: Spring Boot + PostgreSQL.
- Frontend: SPA Vite + React + TypeScript.
- Motor astrológico: `astro-engine` (pendiente, sidecar Node).
- IA: Gemma 4 (pendiente).

El backend se construye primero; el frontend consume contratos estables.

## Estado

- Implementado: autenticación JWT, carta natal, tránsitos, persistencia y tests.
- Pendiente: lecturas IA, endurecimiento, frontend funcional, `astro-engine`.
- `astro-engine` no existe todavía: los endpoints que dependen de él devuelven `503`.

## Estructura

```text
astro-natal-ai/
├── backend/      Spring Boot, PostgreSQL, tests
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
```

No usar `npm` en el frontend; el proyecto usa `pnpm`.

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

1. `astro-engine` + golden test contra astro.com (<1°).
2. Lecturas IA con Gemma 4.
3. Endurecimiento del backend.
4. Frontend: login, onboarding, carta, tránsitos y lecturas.
5. Despliegue.

No implementar funcionalidad del frontend antes de estabilizar los contratos del backend.
