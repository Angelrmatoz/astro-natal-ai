# Frontend — Astro Natal AI

SPA de astrología construida con Vite, React y TypeScript.

## Stack

- Vite 8
- React 19
- TypeScript 7
- React Router
- pnpm

## Requisitos

- Node.js 24+
- pnpm

```powershell
npm.cmd install -g pnpm
```

## Instalación

```powershell
cd frontend
pnpm install
```

## Variables de entorno

Copia el ejemplo:

```powershell
Copy-Item .env.example .env
```

```properties
VITE_API_URL=http://localhost:8080
```

Vite solo expone variables con prefijo `VITE_`. No pongas secretos en variables `VITE_`: terminan en el bundle público.

`.env` está ignorado por git; `.env.example` sí se versiona.

## Scripts

```powershell
pnpm dev       # servidor de desarrollo
pnpm build     # build de producción
pnpm preview   # previsualizar build
pnpm lint      # oxlint
```

## Estructura

```text
frontend/
├── public/
├── src/
│   ├── assets/
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── .env.example
├── index.html
├── package.json
├── pnpm-lock.yaml
└── vite.config.ts
```

## Estado

Scaffold inicial. Pendiente:

- Rutas de login/registro y guard de sesión.
- Cliente HTTP con JWT.
- Onboarding de datos de nacimiento.
- Carta natal, tránsitos y lecturas.
- Tests de componentes y flujos.

El frontend consumirá la API del backend en `/api`. El backend se construye primero; las contrataciones de pantallas se harán cuando los endpoints estén estables.

## Backend local

```powershell
cd backend
docker compose -f docker-compose.db.yml up -d
.\run-dev.cmd
```

API: `http://localhost:8080`
