# Frontend — Astro Natal AI

SPA de astrología construida con Vite, React y TypeScript.

## Stack

- Vite 8
- React 19
- TypeScript 7
- React Router
- Jest + React Testing Library
- Playwright (Chromium y WebKit)
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
pnpm test      # Jest + Testing Library
pnpm test:e2e  # Playwright (Chromium y WebKit)
```

## Estructura

```text
frontend/
├── e2e/              pruebas Playwright
├── public/
├── src/
│   ├── api/          cliente fetch y tipos de la API
│   ├── assets/fonts/ Instrument Serif (OFL)
│   ├── auth/         sesión, contexto y guard de rutas
│   ├── chart/        hook de la carta del usuario
│   ├── components/   layout, shell de autenticación y dial
│   ├── forms/        utilidades de formularios y validación
│   ├── lib/          formato de fechas, horas y posiciones
│   ├── pages/        login, registro, onboarding, carta, tránsitos, lecturas y perfil
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── .env.example
├── index.html
├── jest.config.cjs
├── package.json
├── playwright.config.ts
├── pnpm-lock.yaml
└── vite.config.ts
```

## Diseño

- Modo de producto: **Operate**. Prioriza legibilidad, jerarquía y estados reales sobre decoración.
- Tipografía: Instrument Serif (autoalojada, licencia OFL en `src/assets/fonts/OFL.txt`) para títulos; pila del sistema para texto.
- Paleta en `src/index.css`: tinta fría de fondo, latón como único acento, tema claro y oscuro por `prefers-color-scheme`.
- Sin librería de UI ni CSS. Tokens semánticos, bordes de 1 px, sin sombras.
- Formularios con validación nativa y `:user-invalid`: el error no aparece hasta que el usuario interactúa. `aria-invalid` se sincroniza desde `AuthShell`.
- Movimiento: una sola animación, el arco del dial; se desactiva con `prefers-reduced-motion`.

## Estado

F1–F6 completados: rutas y guard de sesión, registro con datos de nacimiento, carta natal con lectura integral, tránsitos, lecturas diaria/semanal/mensual, perfil editable y pruebas unitarias/E2E. Pendiente:

- Despliegue del frontend (F7).
- CORS y variables de producción.

El frontend consume la API del backend en `/api`; el backend se construye primero.

## Backend local

```powershell
cd backend
docker compose -f docker-compose.db.yml up -d
.\run-dev.cmd
```

API: `http://localhost:8080`
