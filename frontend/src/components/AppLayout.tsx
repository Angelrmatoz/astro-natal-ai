import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

const sections = [
  { to: '/chart', label: 'Carta' },
  { to: '/transits', label: 'Tránsitos' },
  { to: '/readings', label: 'Lecturas' },
]

export function AppLayout() {
  const { session, logout } = useAuth()

  return (
    <div className="app">
      <header className="topbar">
        <Link className="wordmark" to="/dashboard">
          Astro Natal AI
        </Link>

        <nav className="topnav" aria-label="Secciones">
          {sections.map((section) => (
            <NavLink
              key={section.to}
              to={section.to}
              className={({ isActive }) =>
                isActive ? 'topnav__link topnav__link--active' : 'topnav__link'
              }
            >
              {section.label}
            </NavLink>
          ))}
        </nav>

        <div className="topbar__session">
          <Link className="topbar__profile" to="/profile">
            {session?.user.nombre}
          </Link>
          <button type="button" className="text-button" onClick={logout}>
            Salir
          </button>
        </div>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </div>
  )
}
