import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { saveChart } from '../api/astro'
import type { ChartRequest } from '../api/types'
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

type OnboardingState = {
  draft?: ChartRequest
  error?: string
}

export function OnboardingPage() {
  const { session } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const { status } = useMyChart()
  const formRef = useUserInvalidSync<HTMLFormElement>()
  const state = (location.state as OnboardingState | null) ?? {}
  const [error, setError] = useState<string | null>(state.error ?? null)
  const [pending, setPending] = useState(false)

  if (status === 'ready') {
    return <Navigate to="/chart" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!session) {
      return
    }
    setError(null)
    setPending(true)
    try {
      await saveChart(chartRequestFromFormData(new FormData(event.currentTarget)))
      navigate('/chart', { replace: true })
    } catch (cause) {
      setError(chartErrorMessage(cause))
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="stack">
      <header className="page__header">
        <h1 className="display">Tus datos de nacimiento</h1>
        <p className="lede">
          Con la fecha, la hora y el lugar calculamos tu carta natal. La hora es opcional: sin ella
          perdemos el ascendente y las casas, pero no los planetas ni los aspectos.
        </p>
      </header>

      <form className="form form--wide" ref={formRef} onSubmit={handleSubmit} aria-busy={pending}>
        <BirthDataFields initial={state.draft} fechaMax={todayLocal()} />

        {error ? (
          <p className="alert" role="alert">
            {error}
          </p>
        ) : null}

        <button type="submit" className="button" disabled={pending}>
          {pending ? 'Calculando tu carta…' : 'Calcular mi carta'}
        </button>
      </form>
    </div>
  )
}
