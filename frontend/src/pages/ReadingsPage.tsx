import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { generateReading } from '../api/astro'
import { ApiError } from '../api/client'
import type { ChartResponse, ReadingResponse } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { useMyChart } from '../chart/useMyChart'

type Period = 'day' | 'week' | 'month'

const PERIODS: { value: Period; label: string; phrase: string }[] = [
  { value: 'day', label: 'Día', phrase: 'del día' },
  { value: 'week', label: 'Semana', phrase: 'de la semana' },
  { value: 'month', label: 'Mes', phrase: 'del mes' },
]

function readingMessage(cause: unknown) {
  if (cause instanceof ApiError && cause.status === 503) {
    return 'La IA no está disponible ahora. Intenta de nuevo en unos minutos.'
  }
  return cause instanceof Error ? cause.message : 'No se pudo generar la lectura.'
}

function ReadingsDetail({ chart }: { chart: ChartResponse }) {
  const { session } = useAuth()
  const [period, setPeriod] = useState<Period>('day')
  const [reading, setReading] = useState<ReadingResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  const phrase = PERIODS.find((option) => option.value === period)?.phrase ?? 'del día'

  function selectPeriod(next: Period) {
    setPeriod(next)
    setReading(null)
    setError(null)
  }

  async function handleGenerate() {
    if (!session) {
      return
    }
    setError(null)
    setPending(true)
    try {
      setReading(await generateReading(session.token, chart.id, period))
    } catch (cause) {
      setError(readingMessage(cause))
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="stack">
      <header className="page__header">
        <h1 className="display">Tus lecturas</h1>
        <p className="lede">
          Una interpretación de los tránsitos de hoy sobre tu carta. Se guarda: la misma fecha no se
          vuelve a generar.
        </p>
        <p>
          <Link className="text-link" to="/chart">
            Volver a mi carta
          </Link>
        </p>
      </header>

      <div className="segmented" role="group" aria-label="Periodo de la lectura">
        {PERIODS.map((option) => (
          <button
            key={option.value}
            type="button"
            className="segmented__option"
            aria-pressed={period === option.value}
            onClick={() => selectPeriod(option.value)}
          >
            {option.label}
          </button>
        ))}
      </div>

      {reading ? (
        <article className="reading" aria-label={`Lectura ${phrase}`}>
          {reading.contenidoTexto
            .split(/\n{2,}/)
            .map((paragraph) => paragraph.replace(/^#{1,6}\s*/gm, '').replace(/\*/g, '').trim())
            .filter((paragraph) => paragraph !== '')
            .map((paragraph, index) => (
              <p key={index}>{paragraph}</p>
            ))}
        </article>
      ) : null}

      {error ? (
        <p className="alert" role="alert">
          {error}
        </p>
      ) : null}

      <div className="reading__actions">
        {reading ? null : (
          <button type="button" className="button" onClick={handleGenerate} disabled={pending}>
            {pending ? 'Generando… puede tardar un minuto' : `Generar lectura ${phrase}`}
          </button>
        )}
        <span className="muted">Interpretación astrológica, no consejo médico ni legal.</span>
      </div>
    </div>
  )
}

export function ReadingsPage() {
  const { chart, status, error } = useMyChart()

  if (status === 'loading') {
    return <p className="muted">Cargando tu carta…</p>
  }
  if (status === 'missing') {
    return <Navigate to="/onboarding" replace />
  }
  if (!chart) {
    return (
      <p className="alert" role="alert">
        {error ?? 'No se pudo cargar tu carta.'}
      </p>
    )
  }
  return <ReadingsDetail chart={chart} />
}
