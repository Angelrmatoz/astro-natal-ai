import { createServer } from 'node:http';
import { health, natalChart, transits } from './astro.mjs';

const PORT = Number(process.env.PORT ?? process.env.ASTRO_ENGINE_PORT ?? 3001);
const MAX_BODY_BYTES = 1024 * 1024;

class BadRequest extends Error {}

function isValidDate(value) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return false;
  }
  const [year, month, day] = value.split('-').map(Number);
  const date = new Date(Date.UTC(year, month - 1, day));
  return (
    date.getUTCFullYear() === year && date.getUTCMonth() === month - 1 && date.getUTCDate() === day
  );
}

function isValidTime(value) {
  return typeof value === 'string' && /^([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?$/.test(value);
}

function isValidTimezone(value) {
  if (typeof value !== 'string' || value.trim() === '') {
    return false;
  }
  try {
    new Intl.DateTimeFormat('en-US', { timeZone: value });
    return true;
  } catch {
    return false;
  }
}

function requireNumber(value, name, min, max) {
  if (typeof value !== 'number' || !Number.isFinite(value) || value < min || value > max) {
    throw new BadRequest(`"${name}" debe ser un número entre ${min} y ${max}`);
  }
  return value;
}

function parseChartRequest(body) {
  if (!isValidDate(body?.fecha)) {
    throw new BadRequest('"fecha" debe tener formato YYYY-MM-DD');
  }
  const timeKnown = body.time_known;
  if (typeof timeKnown !== 'boolean') {
    throw new BadRequest('"time_known" debe ser booleano');
  }
  if (timeKnown && !isValidTime(body.hora)) {
    throw new BadRequest('"hora" debe tener formato HH:MM cuando time_known es true');
  }
  if (!isValidTimezone(body.timezone)) {
    throw new BadRequest(
      '"timezone" debe ser una zona IANA válida, por ejemplo America/Santo_Domingo',
    );
  }
  return {
    fecha: body.fecha,
    hora: timeKnown ? body.hora.slice(0, 5) : null,
    timeKnown,
    lat: requireNumber(body.lat, 'lat', -90, 90),
    lng: requireNumber(body.lng, 'lng', -180, 180),
    timezone: body.timezone,
  };
}

function parseTransitRequest(body) {
  if (!body?.carta || typeof body.carta !== 'object' || !Array.isArray(body.carta.planets)) {
    throw new BadRequest('"carta" debe ser una carta natal con "planets"');
  }
  const fecha = body.fecha ?? new Date().toISOString().slice(0, 10);
  if (!isValidDate(fecha)) {
    throw new BadRequest('"fecha" debe tener formato YYYY-MM-DD');
  }
  return { carta: body.carta, fecha };
}

function readBody(request) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    request.on('data', (chunk) => {
      size += chunk.length;
      if (size > MAX_BODY_BYTES) {
        reject(new BadRequest('El cuerpo de la petición es demasiado grande'));
        request.destroy();
        return;
      }
      chunks.push(chunk);
    });
    request.on('end', () => {
      const raw = Buffer.concat(chunks).toString('utf8');
      if (raw.trim() === '') {
        resolve({});
        return;
      }
      try {
        resolve(JSON.parse(raw));
      } catch {
        reject(new BadRequest('El cuerpo debe ser JSON válido'));
      }
    });
    request.on('error', reject);
  });
}

function send(response, status, payload) {
  const json = JSON.stringify(payload);
  response.writeHead(status, {
    'content-type': 'application/json; charset=utf-8',
    'content-length': Buffer.byteLength(json),
  });
  response.end(json);
}

async function route(request, response) {
  const path = new URL(request.url, 'http://localhost').pathname;

  if (request.method === 'GET' && path === '/health') {
    send(response, 200, health());
    return;
  }
  if (request.method === 'POST' && path === '/natal-chart') {
    send(response, 200, natalChart(parseChartRequest(await readBody(request))));
    return;
  }
  if (request.method === 'POST' && path === '/transits') {
    const { carta, fecha } = parseTransitRequest(await readBody(request));
    send(response, 200, transits(carta, fecha));
    return;
  }
  send(response, 404, { error: 'Ruta no encontrada' });
}

const server = createServer((request, response) => {
  route(request, response).catch((error) => {
    if (error instanceof BadRequest) {
      send(response, 400, { error: error.message });
      return;
    }
    console.error(error);
    send(response, 500, { error: 'Error interno del motor astrológico' });
  });
});

server.listen(PORT, () => {
  console.log(`astro-engine escuchando en http://localhost:${PORT}`);
});
