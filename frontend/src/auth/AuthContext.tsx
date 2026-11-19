import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { UNAUTHORIZED_EVENT, apiRequest } from '../api/client'
import type { AuthResponse, SessionUser } from '../api/types'
import { AuthContext } from './context'
import { clearSession, loadSession, saveSession } from './session'
import type { Session } from './session'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => loadSession())

  const apply = useCallback((response: AuthResponse) => {
    const next: Session = {
      token: response.token,
      user: response.user,
      expiresAt: response.expiresAt,
    }
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

  const updateProfile = useCallback(
    async (nombre: string) => {
      if (!session) {
        return
      }
      const user = await apiRequest<SessionUser>('/api/users/me', {
        method: 'PATCH',
        body: { nombre },
        token: session.token,
      })
      setSession((current) => {
        if (!current) {
          return current
        }
        const next = { ...current, user }
        saveSession(next)
        return next
      })
    },
    [session],
  )

  const logout = useCallback(() => {
    clearSession()
    setSession(null)
  }, [])

  useEffect(() => {
    const onUnauthorized = () => logout()
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized)
  }, [logout])

  const value = useMemo(
    () => ({ session, login, register, updateProfile, logout }),
    [session, login, register, updateProfile, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
