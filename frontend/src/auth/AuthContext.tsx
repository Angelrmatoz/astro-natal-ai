import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { UNAUTHORIZED_EVENT, apiRequest } from '../api/client'
import type { AuthResponse, SessionUser } from '../api/types'
import { AuthContext } from './context'
import { clearSession, loadSession, saveSession } from './session'
import type { Session } from './session'

let csrfRequest: Promise<unknown> | null = null

function ensureCsrfCookie() {
  if (!csrfRequest) {
    csrfRequest = apiRequest('/api/auth/csrf').catch(() => {
      csrfRequest = null
    })
  }
  return csrfRequest
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => loadSession())
  const [ready, setReady] = useState(false)

  useEffect(() => {
    let cancelled = false

    async function bootstrap() {
      await ensureCsrfCookie()
      if (!session) {
        if (!cancelled) {
          setReady(true)
        }
        return
      }
      try {
        const user = await apiRequest<SessionUser>('/api/auth/me')
        if (!cancelled) {
          const next = { user }
          saveSession(next)
          setSession(next)
        }
      } catch {
        if (!cancelled) {
          clearSession()
          setSession(null)
        }
      } finally {
        if (!cancelled) {
          setReady(true)
        }
      }
    }

    void bootstrap()
    return () => {
      cancelled = true
    }
    // Solo al montar: la sesión inicial se valida contra /auth/me.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const apply = useCallback((response: AuthResponse) => {
    const next: Session = { user: response.user }
    saveSession(next)
    setSession(next)
    return response
  }, [])

  const login = useCallback(
    async (email: string, password: string) => {
      return apply(
        await apiRequest<AuthResponse>('/api/auth/login', {
          method: 'POST',
          body: { email, password },
        }),
      )
    },
    [apply],
  )

  const register = useCallback(
    async (nombre: string, email: string, password: string) => {
      return apply(
        await apiRequest<AuthResponse>('/api/auth/register', {
          method: 'POST',
          body: { nombre, email, password },
        }),
      )
    },
    [apply],
  )

  const updateProfile = useCallback(async (nombre: string, timezone?: string) => {
    const user = await apiRequest<SessionUser>('/api/users/me', {
      method: 'PATCH',
      body: { nombre, ...(timezone ? { timezone } : {}) },
    })
    const next = { user }
    saveSession(next)
    setSession(next)
  }, [])

  const logout = useCallback(() => {
    void apiRequest('/api/auth/logout', { method: 'POST' }).catch(() => undefined)
    clearSession()
    setSession(null)
  }, [])

  useEffect(() => {
    const onUnauthorized = () => logout()
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
  }, [logout])

  const value = useMemo(
    () => ({ session, ready, login, register, updateProfile, logout }),
    [session, ready, login, register, updateProfile, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
