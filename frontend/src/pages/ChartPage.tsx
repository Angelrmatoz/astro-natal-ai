import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { generateReading } from '../api/astro'
import { ApiError } from '../api/client'
import type { ChartResponse, ReadingResponse } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { useMyChart } from '../chart/useMyChart'
import { PlanetTable } from '../components/PlanetTable'
import { formatDate, formatPosition, formatTime } from '../lib/format'

function readingMessage(cause: unknown) {
  if (cause instanceof ApiError && cause.status === 503) {
    return 'El servicio de IA no está disponible ahora. Intenta de nuevo en unos minutos.'
  }
  return cause instanceof Error ? cause.message : 'No se pudo generar la lectura.'
}

function ChartDetail({ chart }: { chart: ChartResponse }) {
  const { session } = useAuth()
  const { datosCarta } = chart
  const [reading, setReading] = useState<ReadingResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  const sun = datosCarta.planets.find((planet) => planet.name === 'Sol') ?? null
  const moon = datosCarta.planets.find((planet) => planet.name === 'Luna') ?? null

  async function handleReading() {
    if (!session) {
      return
    }
    setError(null)
    setPending(true)
    try {
      setReading(await generateReading(chart.id, 'natal'))
    } catch (cause) {
      setError(readingMessage(cause))
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="stack">
      <header className="page__header">
        <h1 className="display">Tu carta natal</h1>
        <p className="lede">
          {formatDate(chart.fechaNac)} · {formatTime(chart.horaNac)} · {chart.ciudad}
        </p>
        <p>
          <Link className="text-link" to="/profile">
            Editar datos de nacimiento
          </Link>
        </p>
      </header>

      <dl className="facts">
        <div>
          <dt>Sol</dt>
          <dd>{formatPosition(sun)}</dd>
        </div>
        <div>
          <dt>Luna</dt>
          <dd>{formatPosition(moon)}</dd>
        </div>
        <div>
          <dt>Ascendente</dt>
          <dd>{formatPosition(datosCarta.ascendant)}</dd>
        </div>
      </dl>

      {!chart.timeKnown ? (
        <p className="notice">
          Esta carta no tiene hora de nacimiento. El ascendente y las casas no se calculan; los
          planetas y los aspectos sí son válidos.
        </p>
      ) : null}

      <section aria-labelledby="planets-title">
        <h2 id="planets-title" className="section-title">
          Planetas
        </h2>
        <PlanetTable planets={datosCarta.planets} />
      </section>

      {datosCarta.houses.length > 0 ? (
        <section aria-labelledby="houses-title">
          <h2 id="houses-title" className="section-title">
            Casas
          </h2>
          <table className="table">
            <thead>
              <tr>
                <th scope="col">Casa</th>
                <th scope="col">Signo</th>
                <th scope="col">Cúspide</th>
              </tr>
            </thead>
            <tbody>
              {datosCarta.houses.map((house) => (
                <tr key={house.number}>
                  <th scope="row">{house.number}</th>
                  <td>{house.sign}</td>
                  <td>{house.cusp}°</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      ) : null}

      <section aria-labelledby="aspects-title">
        <h2 id="aspects-title" className="section-title">
          Aspectos
        </h2>
        {datosCarta.aspects.length > 0 ? (
          <ul className="aspects">
            {datosCarta.aspects.map((aspect) => (
              <li key={`${aspect.a}-${aspect.b}-${aspect.type}`}>
                <span className="aspects__pair">
                  {aspect.a} <span className="aspects__type">{aspect.type}</span> {aspect.b}
                </span>
                <span className="aspects__orb">orbe {aspect.orb.toFixed(1)}°</span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="muted">No hay aspectos dentro del orbe de 6°.</p>
        )}
      </section>

      <section aria-labelledby="reading-title">
        <h2 id="reading-title" className="section-title">
          Lectura de la carta
        </h2>

        {reading ? (
          <div className="reading">
            {reading.contenidoTexto
              .split(/\n{2,}/)
              .map((paragraph) => paragraph.replace(/^#{1,6}\s*/gm, '').replace(/\*/g, '').trim())
              .filter((paragraph) => paragraph !== '')
              .map((paragraph, index) => (
                <p key={index}>{paragraph}</p>
              ))}
          </div>
        ) : (
          <p className="lede">
            Una interpretación de los planetas, las casas y los aspectos de tu carta, redactada con
            Gemma.
          </p>
        )}

        {error ? (
          <p className="alert" role="alert">
            {error}
          </p>
        ) : null}

        <div className="reading__actions">
          {reading ? null : (
            <button type="button" className="button" onClick={handleReading} disabled={pending}>
              {pending ? 'Analizando…' : 'Analizar mi carta'}
            </button>
          )}
          <span className="muted">Interpretación astrológica, no consejo médico ni legal.</span>
        </div>
      </section>
    </div>
  )
}

export function ChartPage() {
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
  return <ChartDetail chart={chart} />
}
