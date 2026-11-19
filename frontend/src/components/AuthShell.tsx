import type { ReactNode } from 'react'
import { EclipticDial } from './EclipticDial'
import { useUserInvalidSync } from '../forms/useUserInvalidSync'

type AuthShellProps = {
  title: string
  lede: string
  children: ReactNode
  footer: ReactNode
}

export function AuthShell({ title, lede, children, footer }: AuthShellProps) {
  const formColumn = useUserInvalidSync<HTMLElement>()

  return (
    <main className="auth">
      <section className="auth__form" ref={formColumn}>
        <p className="wordmark">Astro Natal AI</p>
        <h1 className="display">{title}</h1>
        <p className="lede">{lede}</p>
        {children}
        <footer className="auth__footer">{footer}</footer>
      </section>

      <aside className="auth__aside">
        <EclipticDial />
        <p className="auth__note">Carta natal, tránsitos y lecturas</p>
      </aside>
    </main>
  )
}
