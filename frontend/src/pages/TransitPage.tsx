import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { getTransits } from '../api/astro'
import { ApiError } from '../api/client'
import type { ChartResponse, TransitResponse } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { useMyChart } from '../chart/useMyChart'
import { PlanetTable } from '../components/PlanetTable'
import { chartErrorMessage } from '../forms/chartErrorMessage'
import { formatDate } from '../lib/format'

function TransitDetail({ chart }: { chart: ChartResponse }) {
  const { session } = useAuth()
  const [transits, setTransits] = useState<TransitResponse | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!session) {
      return
    }
    const controller = new AbortController()

    getTransits(controller.signal)
      .then((result) => setTransits(result))
      .catch((cause: unknown) => {
        if (controller.signal.aborted) {
          return
        }
        setError(
          cause instanceof ApiError && cause.status === 503
            ? 'El motor astrológico no está disponible ahora. Intenta de nuevo en unos minutos.'
            : chartErrorMessage(cause),
        )
      })

    return () => controller.abort()
  }, [session, chart.id])

  return (
    <div className="stack">
      <header className="page__header">
        <h1 className="display">Tránsitos de hoy</h1>
        <p className="lede">
          Los planetas de hoy y los aspectos que forman con tu carta natal de {chart.ciudad}.
        </p>
        <p>
          <Link className="text-link" to="/chart">
            Volver a mi carta
          </Link>
        </p>
      </header>

      {error ? (
        <p className="alert" role="alert">
          {error}
        </p>
      ) : null}

      {!transits && !error ? <p className="muted">Calculando los tránsitos…</p> : null}

      {transits ? (
        <>
          <section aria-labelledby="transit-planets-title">
            <h2 id="transit-planets-title" className="section-title">
              Cielo de hoy · {formatDate(transits.fecha)}
            </h2>
            <PlanetTable planets={transits.planets} />
          </section>

          <section aria-labelledby="transit-aspects-title">
            <h2 id="transit-aspects-title" className="section-title">
              Aspectos con tu carta
            </h2>
            {transits.aspects.length > 0 ? (
              <ul className="aspects">
                {transits.aspects.map((aspect) => (
                  <li key={`${aspect.transit}-${aspect.natal}-${aspect.type}`}>
                    <span className="aspects__pair">
                      {aspect.transit} <span className="aspects__type">{aspect.type}</span>{' '}
                      {aspect.natal}
                    </span>
                    <span className="aspects__orb">orbe {aspect.orb.toFixed(1)}°</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="muted">Hoy no hay aspectos dentro del orbe de 6°.</p>
            )}
          </section>
        </>
      ) : null}
    </div>
  )
}

export function TransitPage() {
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
  return <TransitDetail chart={chart} />
}
