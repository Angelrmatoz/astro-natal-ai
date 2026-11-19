import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import { MakeTime } from 'astronomy-engine';
import {
  SIGNS,
  eclipticPointAltitude,
  natalChart,
  transits,
  zonedToUtc,
} from '../src/astro.mjs';

const golden = JSON.parse(
  readFileSync(new URL('./fixtures/horizons-longitudes.json', import.meta.url), 'utf8'),
);

function delta(a, b) {
  return Math.abs(((a - b + 540) % 360) - 180);
}

for (const testCase of golden.cases) {
  const instant = new Date(testCase.instant);
  const chart = natalChart({
    fecha: instant.toISOString().slice(0, 10),
    hora: instant.toISOString().slice(11, 16),
    timeKnown: true,
    lat: testCase.lat,
    lng: testCase.lng,
    timezone: 'UTC',
  });

  test(`posiciones dentro de ${golden.toleranceDegrees}° de Horizons en ${testCase.instant}`, () => {
    for (const planet of chart.planets) {
      const expected = testCase.longitudes[planet.name];
      assert.ok(expected !== undefined, `falta referencia para ${planet.name}`);
      const error = delta(planet.longitude, expected);
      assert.ok(error < golden.toleranceDegrees, `${planet.name}: error ${error.toFixed(4)}°`);
    }
  });

  test(`el ascendente está sobre el horizonte en ${testCase.instant}`, () => {
    const time = MakeTime(instant);
    const altitude = eclipticPointAltitude(
      time,
      testCase.lat,
      testCase.lng,
      chart.ascendant.longitude,
    );
    assert.ok(Math.abs(altitude) < 0.01, `altitud del ascendente ${altitude.toFixed(4)}°`);
  });
}

test('las casas de signo completo empiezan en el signo del ascendente', () => {
  const chart = natalChart({
    fecha: '1990-05-15',
    hora: '14:30',
    timeKnown: true,
    lat: 18.4861,
    lng: -69.9312,
    timezone: 'America/Santo_Domingo',
  });

  assert.equal(chart.houses.length, 12);
  assert.equal(chart.houses[0].sign, chart.ascendant.sign);
  assert.equal(chart.houses[0].cusp, SIGNS.indexOf(chart.ascendant.sign) * 30);
  chart.houses.forEach((house, offset) => {
    assert.equal(house.number, offset + 1);
    assert.equal(house.sign, SIGNS[(SIGNS.indexOf(chart.ascendant.sign) + offset) % 12]);
  });
});

test('sin hora no hay casas ni ascendente', () => {
  const chart = natalChart({
    fecha: '1975-02-20',
    hora: null,
    timeKnown: false,
    lat: 4.711,
    lng: -74.0721,
    timezone: 'America/Bogota',
  });

  assert.deepEqual(chart.houses, []);
  assert.equal(chart.ascendant, null);
  assert.equal(chart.midheaven, null);
  assert.equal(chart.planets.length, 10);
});

test('la hora local se convierte con la zona IANA', () => {
  const madridWinter = zonedToUtc('2024-01-15', '12:00', 'Europe/Madrid');
  const madridSummer = zonedToUtc('2024-07-15', '12:00', 'Europe/Madrid');

  assert.equal(madridWinter.toISOString(), '2024-01-15T11:00:00.000Z');
  assert.equal(madridSummer.toISOString(), '2024-07-15T10:00:00.000Z');
});

test('los tránsitos devuelven planetas y aspectos con orbe acotado', () => {
  const chart = natalChart({
    fecha: '1990-05-15',
    hora: '14:30',
    timeKnown: true,
    lat: 18.4861,
    lng: -69.9312,
    timezone: 'America/Santo_Domingo',
  });
  const result = transits(chart, '2026-11-17');

  assert.equal(result.fecha, '2026-11-17');
  assert.equal(result.planets.length, 10);
  assert.ok(Array.isArray(result.aspects));
  for (const aspect of result.aspects) {
    assert.ok(aspect.orb <= 6, `orbe fuera de rango: ${aspect.orb}`);
    assert.ok(chart.planets.some((planet) => planet.name === aspect.natal));
  }
});
