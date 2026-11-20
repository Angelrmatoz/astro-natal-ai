# astro-engine

Sidecar de cálculo astrológico para Astro Natal AI. Recibe la fecha, hora y ubicación de
nacimiento y devuelve las posiciones planetarias, el ascendente, las casas y los aspectos.

- Runtime: Node.js 24, sin framework HTTP (solo `node:http`).
- Librería astronómica: [`astronomy-engine`](https://github.com/cosinekitty/astronomy) (licencia MIT).
- Sistema de casas: signos completos (`whole-sign`).
- Puerto por defecto: `3001` (`PORT`).

## Comandos

```powershell
cd astro-engine
pnpm install
pnpm test
pnpm start
```

## Contrato

### `GET /health`

```json
{ "status": "ok", "engine": "astronomy-engine" }
```

### `POST /natal-chart`

```json
{
  "fecha": "1990-05-15",
  "hora": "14:30",
  "time_known": true,
  "lat": 18.4861,
  "lng": -69.9312,
  "timezone": "America/Santo_Domingo"
}
```

- `hora` es `null` cuando `time_known` es `false`.
- `timezone` debe ser una zona IANA.
- `houseSystem` se ignora: el motor solo calcula signos completos.

Respuesta:

```json
{
  "meta": { "fecha": "...", "hora": "...", "time_known": true, "lat": 18.48, "lng": -69.93, "timezone": "...", "house_system": "whole-sign", "engine": "astronomy-engine" },
  "planets": [{ "name": "Sol", "sign": "Tauro", "degree": 24.31, "longitude": 54.31, "retrograde": false }],
  "aspects": [{ "a": "Sol", "b": "Luna", "type": "trígono", "orb": 1.24 }],
  "ascendant": { "sign": "Virgo", "degree": 21.24, "longitude": 171.24 },
  "midheaven": { "sign": "Géminis", "degree": 21.54, "longitude": 81.54 },
  "houses": [{ "number": 1, "sign": "Virgo", "cusp": 150 }]
}
```

Sin hora de nacimiento: `ascendant`, `midheaven` son `null` y `houses` es `[]`.

### `POST /transits`

```json
{ "carta": { "planets": [] }, "fecha": "2026-11-17" }
```

`carta` es la respuesta completa de `/natal-chart`. `fecha` es opcional (por defecto, hoy).
Los planetas se calculan a las 12:00 de la zona horaria de la carta.

Respuesta:

```json
{
  "fecha": "2026-11-17",
  "planets": [{ "name": "Marte", "sign": "Leo", "degree": 3.4, "longitude": 123.4, "retrograde": false }],
  "aspects": [{ "transit": "Marte", "natal": "Sol", "type": "cuadratura", "orb": 1.2 }]
}
```

Errores: `400` con `{ "error": "..." }` para datos inválidos, `404` para rutas desconocidas.

## Precisión

- Posiciones geocéntricas aparentes, eclíptica verdadera de la fecha, corregidas por aberración.
- Verificado contra el servicio Horizons de JPL (DE441) con error menor a `0.01°` en tres épocas
  (1950, 1990, 2024), muy por debajo de la tolerancia de `1°` del producto.
- El ascendente se valida geométricamente: el punto calculado queda sobre el horizonte.

## Límites conocidos

- Orbe fijo de `6°` para todos los aspectos; sin orbes por tipo ni por luminaria.
- Solo diez planetas: sin nodos, Quirón, asteroides ni puntos medios.
- Casas de signos completos; no hay Placidus, Koch ni casas por tiempo.
- Sin retrogradación verdadera: se compara la longitud medio día antes y después.
