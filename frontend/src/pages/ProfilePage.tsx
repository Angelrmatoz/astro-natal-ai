import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { saveChart } from '../api/astro'
import type { ChartResponse } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { useMyChart } from '../chart/useMyChart'
import { BirthDataFields } from '../components/BirthDataFields'
import { chartErrorMessage } from '../forms/chartErrorMessage'
import { chartRequestFromFormData } from '../forms/chartRequestFromFormData'
import { useUserInvalidSync } from '../forms/useUserInvalidSync'

function todayLocal() {
  const today = new Date()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${today.getFullYear()}-${month}-${day}`
}

function ProfileForm({ chart }: { chart: ChartResponse }) {
  const { session, updateProfile } = useAuth()
  const navigate = useNavigate()
  const formRef = useUserInvalidSync<HTMLFormElement>()
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!session) {
      return
    }
    const data = new FormData(event.currentTarget)
    const nombre = String(data.get('nombre') ?? '').trim()
    setPending(true)
    setError(null)
    try {
      if (nombre !== session.user.nombre) {
        await updateProfile(nombre)
      }
      await saveChart(session.token, chartRequestFromFormData(data))
      navigate('/chart', { replace: true })
    } catch (cause) {
      setError(chartErrorMessage(cause))
      setPending(false)
    }
  }

  return (
    <div className="stack">
      <header className="page__header">
        <h1 className="display">Tu perfil</h1>
        <p className="lede">Cambia tu nombre o corrige los datos con los que se calculó tu carta.</p>
      </header>

      <form className="form form--wide" ref={formRef} onSubmit={handleSubmit} aria-busy={pending}>
        <div className="form__section">
          <h2 className="section-title">Cuenta</h2>

          <div className="field">
            <label htmlFor="profile-name">Nombre</label>
            <input
              id="profile-name"
              name="nombre"
              type="text"
              defaultValue={session?.user.nombre ?? ''}
              required
              maxLength={120}
              autoComplete="name"
              aria-errormessage="profile-name-error"
            />
            <p className="field__error" id="profile-name-error">
              El nombre no puede quedar vacío.
            </p>
          </div>

          <div className="field">
            <label htmlFor="profile-email">Correo</label>
            <input
              id="profile-email"
              type="email"
              value={session?.user.email ?? ''}
              readOnly
              autoComplete="username"
            />
            <p className="field__hint">El correo no se puede cambiar por ahora.</p>
          </div>
        </div>

        <div className="form__section">
          <h2 className="section-title">Nacimiento</h2>
          <BirthDataFields
            fechaMax={todayLocal()}
            initial={{
              fechaNac: chart.fechaNac,
              horaNac: chart.horaNac,
              timeKnown: chart.timeKnown,
              ciudad: chart.ciudad,
            }}
          />
        </div>

        {error ? (
          <p className="alert" role="alert">
            {error}
          </p>
        ) : null}

        <button type="submit" className="button" disabled={pending}>
          {pending ? 'Guardando…' : 'Guardar cambios'}
        </button>
      </form>
    </div>
  )
}

export function ProfilePage() {
  const { chart, status, error } = useMyChart()

  if (status === 'loading') {
    return <p className="muted">Cargando tu perfil…</p>
  }
  if (status === 'missing') {
    return <Navigate to="/onboarding" replace />
  }
  if (!chart) {
    return (
      <p className="alert" role="alert">
        {error ?? 'No se pudo cargar tu perfil.'}
      </p>
    )
  }
  return <ProfileForm chart={chart} />
}
