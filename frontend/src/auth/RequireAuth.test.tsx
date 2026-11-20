import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { RequireAuth } from './RequireAuth'

jest.mock('../auth/useAuth', () => ({
  useAuth: jest.fn(),
}))

const mockedUseAuth = useAuth as jest.MockedFunction<typeof useAuth>

function renderProtected() {
  return render(
    <MemoryRouter initialEntries={['/chart']}>
      <Routes>
        <Route path="/login" element={<p>Pantalla de acceso</p>} />
        <Route
          path="/chart"
          element={
            <RequireAuth>
              <p>Carta privada</p>
            </RequireAuth>
          }
        />
      </Routes>
    </MemoryRouter>,
  )
}

describe('RequireAuth', () => {
  it('redirects to login without a session', () => {
    mockedUseAuth.mockReturnValue({
      session: null,
      ready: true,
      login: jest.fn(),
      register: jest.fn(),
      updateProfile: jest.fn(),
      logout: jest.fn(),
    })

    renderProtected()

    expect(screen.getByText('Pantalla de acceso')).toBeInTheDocument()
    expect(screen.queryByText('Carta privada')).not.toBeInTheDocument()
  })

  it('renders the protected content with a session', () => {
    mockedUseAuth.mockReturnValue({
      session: {
        user: { id: 'u1', email: 'ana@example.com', nombre: 'Ana' },
      },
      ready: true,
      login: jest.fn(),
      register: jest.fn(),
      updateProfile: jest.fn(),
      logout: jest.fn(),
    })

    renderProtected()

    expect(screen.getByText('Carta privada')).toBeInTheDocument()
  })
})
