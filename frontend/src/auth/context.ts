import { createContext } from 'react'
import type { AuthResponse } from '../api/types'
import type { Session } from './session'

export type AuthContextValue = {
  session: Session | null
  ready: boolean
  login: (email: string, password: string) => Promise<AuthResponse>
  register: (nombre: string, email: string, password: string) => Promise<AuthResponse>
  updateProfile: (nombre: string, timezone?: string) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
