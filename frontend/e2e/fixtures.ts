import type { Page } from '@playwright/test'

export const session = {
  token: 'test-token',
  user: { id: 'u1', email: 'ana@example.com', nombre: 'Ana' },
  expiresAt: '2099-01-01T00:00:00Z',
}

export const chart = {
  id: 'c1',
  nombre: 'Carta natal',
  fechaNac: '1990-05-15',
  horaNac: null,
  timeKnown: false,
  ciudad: 'Santo Domingo',
  latitud: 18.47186,
  longitud: -69.89232,
  timezone: 'America/Santo_Domingo',
  creadoEn: '2026-11-18T12:00:00Z',
  datosCarta: {
    meta: {
      fecha: '1990-05-15',
      hora: null,
      time_known: false,
      lat: 18.47186,
      lng: -69.89232,
      timezone: 'America/Santo_Domingo',
      house_system: 'whole-sign',
      engine: 'astronomy-engine',
    },
    planets: [
      { name: 'Sol', sign: 'Tauro', degree: 24.5, longitude: 54.5, retrograde: false },
      { name: 'Luna', sign: 'Libra', degree: 3.2, longitude: 183.2, retrograde: false },
    ],
    aspects: [{ a: 'Sol', b: 'Luna', type: 'trígono', orb: 1.3 }],
    ascendant: null,
    midheaven: null,
    houses: [],
  },
}

export async function seedSession(page: Page) {
  await page.addInitScript((value) => {
    localStorage.setItem('astro-natal-ai.session', JSON.stringify(value))
  }, session)
}
