import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { LoginPage } from './LoginPage'

jest.mock('../auth/useAuth', () => ({
  useAuth: jest.fn(),
}))

const mockedUseAuth = useAuth as jest.MockedFunction<typeof useAuth>

describe('LoginPage', () => {
  it('sends the credentials and shows the server error', async () => {
    const login = jest.fn().mockRejectedValue(new Error('Email o contraseña incorrectos'))
    mockedUseAuth.mockReturnValue({
      session: null,
      ready: true,
      login,
      register: jest.fn(),
      updateProfile: jest.fn(),
      logout: jest.fn(),
    })

    const user = userEvent.setup()
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Correo'), 'ana@example.com')
    await user.type(screen.getByLabelText('Contraseña'), 'secret123')
    await user.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(login).toHaveBeenCalledWith('ana@example.com', 'secret123')
    expect(await screen.findByRole('alert')).toHaveTextContent('Email o contraseña incorrectos')
  })
})
