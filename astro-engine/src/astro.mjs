import {
  Body,
  EquatorFromVector,
  GeoVector,
  Horizon,
  MakeTime,
  Observer,
  RotateVector,
  Rotation_ECT_EQJ,
  Rotation_EQJ_ECT,
  Rotation_EQJ_EQD,
  SiderealTime,
  Vector,
  e_tilt,
} from 'astronomy-engine';

const DEG = Math.PI / 180;

export const PLANETS = [
  ['Sol', Body.Sun],
  ['Luna', Body.Moon],
  ['Mercurio', Body.Mercury],
  ['Venus', Body.Venus],
  ['Marte', Body.Mars],
  ['Júpiter', Body.Jupiter],
  ['Saturno', Body.Saturn],
  ['Urano', Body.Uranus],
  ['Neptuno', Body.Neptune],
  ['Plutón', Body.Pluto],
];

export const SIGNS = [
  'Aries',
  'Tauro',
  'Géminis',
  'Cáncer',
  'Leo',
  'Virgo',
  'Libra',
  'Escorpio',
  'Sagitario',
  'Capricornio',
  'Acuario',
  'Piscis',
];

export const ASPECTS = [
  ['conjunción', 0],
  ['sextil', 60],
  ['cuadratura', 90],
  ['trígono', 120],
  ['oposición', 180],
];

// ponytail: orbe fijo de 6 grados para todos los aspectos; separar por tipo si hace falta precisión.
const ORB = 6;

const ENGINE = 'astronomy-engine';

function normalize(degrees) {
  return ((degrees % 360) + 360) % 360;
}

function signedDelta(degrees) {
  return ((degrees + 540) % 360) - 180;
}

function round(value, digits = 4) {
  return Number(value.toFixed(digits));
}

function signIndex(longitude) {
  return Math.floor(normalize(longitude) / 30);
}

function position(longitude) {
  const index = signIndex(longitude);
  return {
    longitude: round(normalize(longitude)),
    sign: SIGNS[index],
    degree: round(normalize(longitude) - index * 30, 2),
  };
}

/** Convierte fecha/hora local a un instante UTC usando la zona IANA. */
export function zonedToUtc(fecha, hora, timezone) {
  const [year, month, day] = fecha.split('-').map(Number);
  const [hour, minute] = (hora ?? '12:00').split(':').map(Number);
  const wallClock = Date.UTC(year, month - 1, day, hour, minute);

  const offset = (instantMs) => {
    const parts = new Intl.DateTimeFormat('en-US', {
      timeZone: timezone,
      hourCycle: 'h23',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    }).formatToParts(new Date(instantMs));
    const value = Object.fromEntries(parts.map((part) => [part.type, part.value]));
    const asUtc = Date.UTC(
      Number(value.year),
      Number(value.month) - 1,
      Number(value.day),
      Number(value.hour),
      Number(value.minute),
      Number(value.second),
    );
    return asUtc - instantMs;
  };

  let instant = wallClock - offset(wallClock);
  const corrected = wallClock - offset(instant);
  if (corrected !== instant) {
    instant = corrected;
  }
  return new Date(instant);
}

function eclipticLongitude(body, time) {
  const vector = RotateVector(Rotation_EQJ_ECT(time), GeoVector(body, time, true));
  return {
    longitude: normalize((Math.atan2(vector.y, vector.x) / DEG)),
    latitude: Math.asin(vector.z / Math.hypot(vector.x, vector.y, vector.z)) / DEG,
  };
}

function isRetrograde(body, time) {
  if (body === Body.Sun || body === Body.Moon) {
    return false;
  }
  const before = eclipticLongitude(body, time.AddDays(-0.5)).longitude;
  const after = eclipticLongitude(body, time.AddDays(0.5)).longitude;
  return signedDelta(after - before) < 0;
}

function eclipticPointAltitude(time, latitude, longitude, eclipticLongitude) {
  const observer = new Observer(latitude, longitude, 0);
  const point = new Vector(
    Math.cos(eclipticLongitude * DEG),
    Math.sin(eclipticLongitude * DEG),
    0,
    time,
  );
  const eqj = RotateVector(Rotation_ECT_EQJ(time), point);
  const eqd = RotateVector(Rotation_EQJ_EQD(time), eqj);
  const { ra, dec } = EquatorFromVector(eqd);
  return Horizon(time, observer, ra, dec, null).altitude;
}

/** Ascendente y Medio Cielo para la latitud/longitud dadas. */
export function angles(time, latitude, longitude) {
  const ramc = normalize(SiderealTime(time) * 15 + longitude);
  const obliquity = e_tilt(time).tobl * DEG;
  const phi = latitude * DEG;
  const ramcRad = ramc * DEG;

  const ascendant = normalize(
    Math.atan2(
      Math.cos(ramcRad),
      -(Math.sin(ramcRad) * Math.cos(obliquity) + Math.tan(phi) * Math.sin(obliquity)),
    ) / DEG,
  );
  const midheaven = normalize(
    Math.atan2(Math.sin(ramcRad), Math.cos(ramcRad) * Math.cos(obliquity)) / DEG,
  );

  return { ascendant, midheaven };
}

function wholeSignHouses(ascendant) {
  const first = signIndex(ascendant);
  return Array.from({ length: 12 }, (_, offset) => {
    const index = (first + offset) % 12;
    return {
      number: offset + 1,
      sign: SIGNS[index],
      cusp: round(index * 30),
    };
  });
}

function planetPositions(time) {
  return PLANETS.map(([name, body]) => {
    const { longitude } = eclipticLongitude(body, time);
    return {
      name,
      ...position(longitude),
      retrograde: isRetrograde(body, time),
    };
  });
}

function findAspects(points, labelA = 'a', labelB = 'b') {
  const found = [];
  for (let i = 0; i < points.length; i += 1) {
    for (let j = i + 1; j < points.length; j += 1) {
      const separation = Math.abs(signedDelta(points[i].longitude - points[j].longitude));
      for (const [type, exact] of ASPECTS) {
        const orb = Math.abs(separation - exact);
        if (orb <= ORB) {
          found.push({
            [labelA]: points[i].name,
            [labelB]: points[j].name,
            type,
            orb: round(orb, 2),
          });
        }
      }
    }
  }
  return found;
}

export function natalChart({ fecha, hora, timeKnown, lat, lng, timezone }) {
  const time = MakeTime(zonedToUtc(fecha, timeKnown ? hora : null, timezone));
  const planets = planetPositions(time);

  const chart = {
    meta: {
      fecha,
      hora: timeKnown ? hora : null,
      time_known: timeKnown,
      lat,
      lng,
      timezone,
      house_system: 'whole-sign',
      engine: ENGINE,
    },
    planets,
    aspects: findAspects(planets),
  };

  if (timeKnown) {
    const { ascendant, midheaven } = angles(time, lat, lng);
    chart.ascendant = position(ascendant);
    chart.midheaven = position(midheaven);
    chart.houses = wholeSignHouses(ascendant);
  } else {
    chart.ascendant = null;
    chart.midheaven = null;
    chart.houses = [];
  }

  return chart;
}

export function transits(chart, fecha) {
  const timezone = chart?.meta?.timezone ?? 'UTC';
  const time = MakeTime(zonedToUtc(fecha, '12:00', timezone));
  const current = planetPositions(time);

  const natal = (chart?.planets ?? []).map((planet) => ({
    name: planet.name,
    longitude: planet.longitude,
  }));

  const pairs = [];
  for (const transit of current) {
    for (const natalPlanet of natal) {
      pairs.push({
        transit,
        natalPlanet,
      });
    }
  }

  const aspects = [];
  for (const { transit, natalPlanet } of pairs) {
    const separation = Math.abs(signedDelta(transit.longitude - natalPlanet.longitude));
    for (const [type, exact] of ASPECTS) {
      const orb = Math.abs(separation - exact);
      if (orb <= ORB) {
        aspects.push({
          transit: transit.name,
          natal: natalPlanet.name,
          type,
          orb: round(orb, 2),
        });
      }
    }
  }

  return { fecha, planets: current, aspects };
}

export function health() {
  return { status: 'ok', engine: ENGINE };
}

export { eclipticPointAltitude, eclipticLongitude };
