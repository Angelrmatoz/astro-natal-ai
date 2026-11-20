import { expect, test } from '@playwright/test'
import { chart, session } from './fixtures'

test('creates the account and the chart in one flow', async ({ page }) => {
  let chartPayload: Record<string, unknown> | null = null

  await page.route('**/api/auth/register', (route) =>
    route.fulfill({
      status: 201,
      contentType: 'application/json',
      body: JSON.stringify({ ...session, tokenType: 'Bearer' }),
    }),
  )
  await page.route('**/api/auth/csrf', (route) => route.fulfill({ status: 204, body: '' }))
  await page.route('**/api/geocoding/search**', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([
        {
          ciudad: 'Santo Domingo, Distrito Nacional, República Dominicana',
          latitud: 18.47186,
          longitud: -69.89232,
          timezone: 'America/Santo_Domingo',
        },
      ]),
    }),
  )
  await page.route('**/api/charts', async (route) => {
    if (route.request().method() === 'POST') {
      chartPayload = route.request().postDataJSON()
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(chart),
      })
      return
    }
    await route.fallback()
  })
  await page.route('**/api/charts/me', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(chart),
    }),
  )

  await page.goto('/register')
  await page.getByLabel('Nombre').fill('Ana')
  await page.getByLabel('Correo').fill('ana@example.com')
  await page.getByLabel('Contraseña').fill('secret12345678')
  await page.getByLabel('Fecha de nacimiento').fill('1990-05-15')
  await page.getByLabel('No sé mi hora de nacimiento').check()
  await expect(page.getByLabel('Hora local')).toBeDisabled()
  await page.getByLabel('Ciudad de nacimiento').fill('Santo')
  await page.getByRole('option', { name: /Santo Domingo/ }).click()

  await page.getByRole('button', { name: 'Crear cuenta y calcular mi carta' }).click()

  await expect(page).toHaveURL(/\/chart$/)
  await expect(page.getByRole('heading', { name: 'Tu carta natal' })).toBeVisible()
  expect(chartPayload).toMatchObject({
    fechaNac: '1990-05-15',
    horaNac: null,
    timeKnown: false,
    latitud: 18.47186,
    longitud: -69.89232,
    timezone: 'America/Santo_Domingo',
  })
})
