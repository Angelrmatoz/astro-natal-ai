import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { useMyChart } from '../chart/useMyChart'

export function DashboardPage() {
  const { session } = useAuth()
  const { chart, status, error } = useMyChart()

  return (
    <div className="stack">
      <header className="page__header">
        <h1 className="display">Hola, {session?.user.nombre}</h1>
        {status === 'loading' ? (
          <p className="lede">Buscando tu carta natal…</p>
        ) : chart ? (
          <p className="lede">
            Tu carta está calculada para el {chart.fechaNac} en {chart.ciudad}.
          </p>
        ) : (
          <p className="lede">
            Falta el dato que abre todo: la fecha, la hora y el lugar en que naciste.
          </p>
        )}
      </header>

      {error ? (
        <p className="alert" role="alert">
          {error}
        </p>
      ) : null}

      <div>
        {chart ? (
          <Link className="button" to="/chart">
            Ver mi carta natal
          </Link>
        ) : (
          <Link className="button" to="/onboarding">
            Completar mis datos de nacimiento
          </Link>
        )}
      </div>

      <section aria-labelledby="journey-title">
        <h2 id="journey-title" className="section-title">
          Lo que se desbloquea
        </h2>
        <ul className="journey">
          <li>
            <h3>Carta natal</h3>
            <p>Posiciones planetarias, ascendente y casas calculadas con tus datos de nacimiento.</p>
            <span className="journey__status">{chart ? 'Listo' : 'Pendiente'}</span>
          </li>
          <li>
            <h3>Tránsitos</h3>
            <p>Los planetas de hoy y los aspectos que forman con tu carta.</p>
            <span className="journey__status">{chart ? 'Disponible' : 'Pendiente'}</span>
          </li>
          <li>
            <h3>Lecturas</h3>
            <p>Interpretación de tu carta y, más adelante, lectura diaria, semanal y mensual.</p>
            <span className="journey__status">{chart ? 'Disponible' : 'Pendiente'}</span>
          </li>
        </ul>
      </section>
    </div>
  )
}
