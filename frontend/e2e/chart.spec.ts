import { expect, test } from '@playwright/test'
import { chart, seedSession } from './fixtures'

test('shows the chart without houses when the hour is unknown', async ({ page }) => {
  await seedSession(page)
  await page.route('**/api/charts/me', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(chart),
    }),
  )

  await page.goto('/chart')

  await expect(page.getByRole('heading', { name: 'Tu carta natal' })).toBeVisible()
  await expect(page.getByRole('rowheader', { name: 'Sol' })).toBeVisible()
  await expect(page.getByRole('cell', { name: '24° 30′ Tauro' })).toBeVisible()
  await expect(page.getByText('Sin hora', { exact: true })).toBeVisible()
  await expect(page.getByText(/no tiene hora de nacimiento/)).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Casas' })).toHaveCount(0)
  await expect(page.getByRole('listitem').filter({ hasText: 'Sol trígono Luna' })).toBeVisible()
})
