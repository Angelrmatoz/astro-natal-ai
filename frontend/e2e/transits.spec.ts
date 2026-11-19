import { expect, test } from '@playwright/test'
import { chart, seedSession } from './fixtures'

const transits = {
  fecha: '2026-11-18',
  planets: [
    { name: 'Marte', sign: 'Leo', degree: 3.4, longitude: 123.4, retrograde: false },
    { name: 'Venus', sign: 'Libra', degree: 12.1, longitude: 192.1, retrograde: true },
  ],
  aspects: [{ transit: 'Marte', natal: 'Sol', type: 'cuadratura', orb: 1.2 }],
}

test('shows today transits and their aspects to the chart', async ({ page }) => {
  await seedSession(page)
  await page.route('**/api/charts/me', (route) =>
    route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(chart) }),
  )
  await page.route('**/api/transits/current', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(transits),
    }),
  )

  await page.goto('/transits')

  await expect(page.getByRole('heading', { name: 'Tránsitos de hoy' })).toBeVisible()
  await expect(page.getByRole('rowheader', { name: 'Marte' })).toBeVisible()
  await expect(page.getByText('Marte cuadratura Sol')).toBeVisible()
  await expect(page.getByText(/orbe 1\.2°/)).toBeVisible()
})
