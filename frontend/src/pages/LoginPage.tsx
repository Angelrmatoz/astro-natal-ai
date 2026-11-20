import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { AuthShell } from '../components/AuthShell'
import { PasswordField } from '../components/PasswordField'

export function LoginPage() {
  const { session, login } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  const from =
    typeof (location.state as { from?: unknown } | null)?.from === 'string'
      ? (location.state as { from: string }).from
      : '/dashboard'

  if (session) {
    return <Navigate to={from} replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    setError(null)
    setPending(true)
    try {
      await login(String(form.get('email') ?? ''), String(form.get('password') ?? ''))
      navigate(from, { replace: true })
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo iniciar sesión')
    } finally {
      setPending(false)
    }
  }

  return (
    <AuthShell
      title="Inicia sesión"
      lede="Tu carta natal y tus lecturas te esperan."
      footer={
        <p>
          ¿Aún no tienes cuenta? <Link to="/register">Crea una</Link>
        </p>
      }
    >
      <form className="form" onSubmit={handleSubmit} aria-busy={pending}>
        <div className="field">
          <label htmlFor="login-email">Correo</label>
          <input
            id="login-email"
            name="email"
            type="email"
            autoComplete="username"
            required
            maxLength={320}
            spellCheck={false}
            autoCapitalize="none"
            enterKeyHint="next"
            aria-errormessage="login-email-error"
          />
          <p className="field__error" id="login-email-error">
            Escribe tu correo completo, por ejemplo nombre@dominio.com.
          </p>
        </div>

        <PasswordField
          name="password"
          label="Contraseña"
          autoComplete="current-password"
          error="Escribe tu contraseña para continuar."
        />

        {error ? (
          <p className="alert" role="alert">
            {error}
          </p>
        ) : null}

        <button type="submit" className="button button--block" disabled={pending}>
          {pending ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </AuthShell>
  )
}
