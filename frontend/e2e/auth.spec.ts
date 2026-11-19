import { expect, test } from '@playwright/test'
import { session } from './fixtures'

test('redirects to login when there is no session', async ({ page }) => {
  await page.goto('/chart')

  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: 'Inicia sesión' })).toBeVisible()
})

test('logs in and lands on the dashboard', async ({ page }) => {
  await page.route('**/api/auth/login', (route) =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ ...session, tokenType: 'Bearer' }),
    }),
  )
  await page.route('**/api/charts/me', (route) =>
    route.fulfill({
      status: 404,
      contentType: 'application/json',
      body: JSON.stringify({ title: 'Carta no encontrada', status: 404 }),
    }),
  )

  await page.goto('/login')
  await page.getByLabel('Correo').fill('ana@example.com')
  await page.getByLabel('Contraseña').fill('secret123')
  await page.getByRole('button', { name: 'Entrar' }).click()

  await expect(page).toHaveURL(/\/dashboard$/)
  await expect(page.getByRole('heading', { name: 'Hola, Ana' })).toBeVisible()
  await expect(page.getByRole('link', { name: 'Completar mis datos de nacimiento' })).toBeVisible()
})
