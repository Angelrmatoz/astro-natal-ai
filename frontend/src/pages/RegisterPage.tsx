import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { saveChart } from '../api/astro'
import { useAuth } from '../auth/useAuth'
import { AuthShell } from '../components/AuthShell'
import { BirthDataFields } from '../components/BirthDataFields'
import { PasswordField } from '../components/PasswordField'
import { chartErrorMessage } from '../forms/chartErrorMessage'
import { chartRequestFromFormData } from '../forms/chartRequestFromFormData'

function todayLocal() {
  const today = new Date()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${today.getFullYear()}-${month}-${day}`
}

export function RegisterPage() {
  const { session, register } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)
  const [created, setCreated] = useState(false)

  if (session && !created && !pending) {
    return <Navigate to="/dashboard" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    const chartRequest = chartRequestFromFormData(data)
    setError(null)
    setPending(true)

    try {
      await register(
        String(data.get('nombre') ?? ''),
        String(data.get('email') ?? ''),
        String(data.get('password') ?? ''),
      )
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo crear la cuenta')
      setPending(false)
      return
    }

    setCreated(true)
    try {
      await saveChart(chartRequest)
      navigate('/chart', { replace: true })
    } catch (cause) {
      navigate('/onboarding', {
        replace: true,
        state: { error: chartErrorMessage(cause), draft: chartRequest },
      })
    }
  }

  return (
    <AuthShell
      title="Crea tu cuenta"
      lede="Con tu fecha y lugar de nacimiento calculamos tu carta. Puedes editar estos datos después."
      footer={
        <p>
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
        </p>
      }
    >
      <form className="form" onSubmit={handleSubmit} aria-busy={pending}>
        <div className="field">
          <label htmlFor="register-name">Nombre</label>
          <input
            id="register-name"
            name="nombre"
            type="text"
            autoComplete="name"
            required
            maxLength={120}
            enterKeyHint="next"
            aria-errormessage="register-name-error"
          />
          <p className="field__error" id="register-name-error">
            Escribe tu nombre o el que quieras usar en la app.
          </p>
        </div>

        <div className="field">
          <label htmlFor="register-email">Correo</label>
          <input
            id="register-email"
            name="email"
            type="email"
            autoComplete="username"
            required
            maxLength={320}
            spellCheck={false}
            autoCapitalize="none"
            enterKeyHint="next"
            aria-errormessage="register-email-error"
          />
          <p className="field__error" id="register-email-error">
            Escribe tu correo completo, por ejemplo nombre@dominio.com.
          </p>
        </div>

        <PasswordField
          name="password"
          label="Contraseña"
          autoComplete="new-password"
          hint="Mínimo 12 caracteres."
          minLength={12}
          error="La contraseña necesita al menos 12 caracteres."
        />

        <div className="form__section">
          <h2 className="section-title">Datos de nacimiento</h2>
          <BirthDataFields fechaMax={todayLocal()} />
        </div>

        {error ? (
          <p className="alert" role="alert">
            {error}
          </p>
        ) : null}

        <button type="submit" className="button button--block" disabled={pending}>
          {pending ? 'Creando tu carta…' : 'Crear cuenta y calcular mi carta'}
        </button>
      </form>
    </AuthShell>
  )
}
