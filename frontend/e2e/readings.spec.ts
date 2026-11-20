import { expect, test } from '@playwright/test'
import { chart, mockSessionApi, seedSession } from './fixtures'

test('generates a daily reading for the stored chart', async ({ page }) => {
  await seedSession(page)
  await mockSessionApi(page)
  await page.route('**/api/charts/me', (route) =>
    route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(chart) }),
  )
  await page.route('**/api/readings/current**', (route) =>
    route.fulfill({
      status: 404,
      contentType: 'application/json',
      body: JSON.stringify({ title: 'Lectura no encontrada', status: 404 }),
    }),
  )
  await page.route('**/api/readings', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 'r1',
        chartId: 'c1',
        tipo: 'day',
        rangoInicio: '2026-11-18',
        rangoFin: '2026-11-18',
        contenidoTexto: 'Hoy tu Sol recibe un trígono de la Luna.\n\nAprovecha la mañana para ordenar ideas.',
        modelo: 'gemma-4-31b-it',
        creadoEn: '2026-11-18T12:00:00Z',
      }),
    }),
  )

  await page.goto('/readings')
  await page.getByRole('button', { name: 'Generar lectura del día' }).click()

  await expect(page.getByText(/Hoy tu Sol recibe un trígono/)).toBeVisible()
  await expect(page.getByText(/Aprovecha la mañana/)).toBeVisible()
  await expect(page.getByText(/Podrás generar otra el/)).toBeVisible()
  await expect(page.getByRole('button', { name: 'Generar lectura del día' })).toHaveCount(0)
})
