import { fireEvent, render, screen } from '@testing-library/react'
import { BirthDataFields } from './BirthDataFields'

jest.mock('../api/astro', () => ({
  searchCities: jest.fn().mockResolvedValue([]),
}))

function renderFields() {
  return render(
    <form>
      <BirthDataFields />
    </form>,
  )
}

describe('BirthDataFields', () => {
  it('requires the time when the hour is known', () => {
    renderFields()

    expect(screen.getByLabelText('Hora local')).toBeRequired()
    expect(screen.getByLabelText('Hora local')).toBeEnabled()
    expect(document.querySelector('input[name="timeKnown"]')).toHaveValue('true')
  })

  it('disables the time and marks timeKnown=false when the hour is unknown', () => {
    renderFields()

    fireEvent.click(screen.getByLabelText('No sé mi hora de nacimiento'))

    expect(screen.getByLabelText('Hora local')).toBeDisabled()
    expect(screen.getByLabelText('Hora local')).not.toBeRequired()
    expect(document.querySelector('input[name="timeKnown"]')).toHaveValue('false')
  })

  it('prefills the saved birth data', () => {
    render(
      <form>
        <BirthDataFields
          initial={{
            fechaNac: '1990-05-15',
            horaNac: '14:30:00',
            timeKnown: true,
            ciudad: 'Santo Domingo',
            latitud: 18.47186,
            longitud: -69.89232,
            timezone: 'America/Santo_Domingo',
          }}
        />
      </form>,
    )

    expect(screen.getByLabelText('Fecha de nacimiento')).toHaveValue('1990-05-15')
    expect(screen.getByLabelText('Hora local')).toHaveValue('14:30')
    expect(screen.getByLabelText('Ciudad de nacimiento')).toHaveValue('Santo Domingo')
    expect(document.querySelector('input[name="latitud"]')).toHaveValue('18.47186')
    expect(document.querySelector('input[name="timezone"]')).toHaveValue('America/Santo_Domingo')
  })
})
