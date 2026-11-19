import type { SessionUser } from '../api/types'

const STORAGE_KEY = 'astro-natal-ai.session'

export type Session = {
  token: string
  user: SessionUser
  expiresAt: string
}

function isSession(value: unknown): value is Session {
  if (typeof value !== 'object' || value === null) {
    return false
  }
  const candidate = value as Partial<Session>
  return (
    typeof candidate.token === 'string' &&
    typeof candidate.expiresAt === 'string' &&
    typeof candidate.user === 'object' &&
    candidate.user !== null &&
    typeof candidate.user.email === 'string'
  )
}

export function loadSession(): Session | null {
  const raw = window.localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return null
  }
  try {
    const parsed: unknown = JSON.parse(raw)
    if (!isSession(parsed) || Date.parse(parsed.expiresAt) <= Date.now()) {
      window.localStorage.removeItem(STORAGE_KEY)
      return null
    }
    return parsed
  } catch {
    window.localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export function saveSession(session: Session): void {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

export function clearSession(): void {
  window.localStorage.removeItem(STORAGE_KEY)
}
