import { useEffect, useState } from 'react'
import type { KeyboardEvent } from 'react'
import { searchCities } from '../api/astro'
import type { GeocodingSuggestion } from '../api/types'

type BirthDataFieldsProps = {
  initial?: {
    fechaNac: string
    horaNac: string | null
    timeKnown: boolean
    ciudad: string
    latitud?: number
    longitud?: number
    timezone?: string
  }
  fechaMax?: string
}

function initialSelection(initial?: BirthDataFieldsProps['initial']): GeocodingSuggestion | null {
  if (
    !initial ||
    initial.latitud === undefined ||
    initial.longitud === undefined ||
    !initial.timezone
  ) {
    return null
  }
  return {
    ciudad: initial.ciudad,
    latitud: initial.latitud,
    longitud: initial.longitud,
    timezone: initial.timezone,
  }
}

export function BirthDataFields({ initial, fechaMax }: BirthDataFieldsProps) {
  const [unknownTime, setUnknownTime] = useState(initial ? !initial.timeKnown : false)
  const [city, setCity] = useState(initial?.ciudad ?? '')
  const [selection, setSelection] = useState<GeocodingSuggestion | null>(() =>
    initialSelection(initial),
  )
  const [suggestions, setSuggestions] = useState<GeocodingSuggestion[]>([])
  const [open, setOpen] = useState(false)
  const [highlighted, setHighlighted] = useState(-1)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (selection) {
      return
    }
    const query = city.trim()
    if (query.length < 2) {
      return
    }

    const controller = new AbortController()
    const timer = window.setTimeout(() => {
      setLoading(true)
      searchCities(query, controller.signal)
        .then((results) => {
          setSuggestions(results)
          setOpen(results.length > 0)
          setHighlighted(-1)
        })
        .catch(() => {
          if (!controller.signal.aborted) {
            setSuggestions([])
            setOpen(false)
          }
        })
        .finally(() => {
          if (!controller.signal.aborted) {
            setLoading(false)
          }
        })
    }, 300)

    return () => {
      window.clearTimeout(timer)
      controller.abort()
    }
  }, [city, selection])

  function chooseSuggestion(suggestion: GeocodingSuggestion) {
    setCity(suggestion.ciudad)
    setSelection(suggestion)
    setSuggestions([])
    setOpen(false)
    setHighlighted(-1)
  }

  function handleCityChange(value: string) {
    setCity(value)
    setSelection(null)
    setHighlighted(-1)
    if (value.trim().length < 2) {
      setSuggestions([])
      setOpen(false)
    }
  }

  function handleCityKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      if (!open && suggestions.length > 0) {
        setOpen(true)
        return
      }
      setHighlighted((current) => (current + 1) % Math.max(suggestions.length, 1))
    } else if (event.key === 'ArrowUp') {
      event.preventDefault()
      setHighlighted((current) =>
        current <= 0 ? suggestions.length - 1 : current - 1,
      )
    } else if (event.key === 'Enter' && open && highlighted >= 0) {
      event.preventDefault()
      const suggestion = suggestions[highlighted]
      if (suggestion) {
        chooseSuggestion(suggestion)
      }
    } else if (event.key === 'Escape') {
      setOpen(false)
      setHighlighted(-1)
    }
  }

  return (
    <>
      <input type="hidden" name="timeKnown" value={unknownTime ? 'false' : 'true'} />
      <input type="hidden" name="latitud" value={selection?.latitud ?? ''} />
      <input type="hidden" name="longitud" value={selection?.longitud ?? ''} />
      <input type="hidden" name="timezone" value={selection?.timezone ?? ''} />

      <div className="field">
        <label htmlFor="birth-date">Fecha de nacimiento</label>
        <input
          id="birth-date"
          name="fechaNac"
          type="date"
          max={fechaMax}
          defaultValue={initial?.fechaNac ?? ''}
          required
          autoComplete="bday"
          aria-errormessage="birth-date-error"
        />
        <p className="field__error" id="birth-date-error">
          Elige una fecha válida, anterior a hoy.
        </p>
      </div>

      <fieldset className="fieldset">
        <legend>Hora de nacimiento</legend>

        <div className="field">
          <label htmlFor="birth-time">Hora local</label>
          <input
            id="birth-time"
            name="horaNac"
            type="time"
            defaultValue={initial?.horaNac ? initial.horaNac.slice(0, 5) : ''}
            required={!unknownTime}
            disabled={unknownTime}
            autoComplete="off"
            aria-errormessage="birth-time-error"
          />
          <p className="field__error" id="birth-time-error">
            Escribe la hora, o marca que no la sabes.
          </p>
        </div>

        <label className="check">
          <input
            type="checkbox"
            checked={unknownTime}
            onChange={(event) => setUnknownTime(event.target.checked)}
          />
          No sé mi hora de nacimiento
        </label>

        <p className="field__hint">
          Sin hora no hay ascendente ni casas. La carta se calcula igual, con planetas y aspectos.
        </p>
      </fieldset>

      <div className="field">
        <label htmlFor="birth-city">Ciudad de nacimiento</label>
        <input
          id="birth-city"
          name="ciudad"
          type="text"
          value={city}
          onChange={(event) => handleCityChange(event.target.value)}
          onKeyDown={handleCityKeyDown}
          required
          maxLength={160}
          autoComplete="off"
          autoCapitalize="words"
          spellCheck={false}
          placeholder="Santo Domingo, República Dominicana"
          role="combobox"
          aria-autocomplete="list"
          aria-expanded={open}
          aria-controls="birth-city-list"
          aria-activedescendant={
            highlighted >= 0 ? `birth-city-option-${highlighted}` : undefined
          }
          aria-describedby="birth-city-hint"
          aria-errormessage="birth-city-error"
        />

        {open ? (
          <ul className="suggestions" id="birth-city-list" role="listbox" aria-label="Ciudades">
            {suggestions.map((suggestion, index) => (
              <li
                key={`${suggestion.ciudad}-${suggestion.latitud}-${suggestion.longitud}`}
                id={`birth-city-option-${index}`}
                role="option"
                aria-selected={index === highlighted}
                className={
                  index === highlighted ? 'suggestions__option is-active' : 'suggestions__option'
                }
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => chooseSuggestion(suggestion)}
              >
                <span>{suggestion.ciudad}</span>
                <span className="suggestions__coords">
                  {suggestion.latitud.toFixed(2)}°, {suggestion.longitud.toFixed(2)}°
                </span>
              </li>
            ))}
          </ul>
        ) : null}

        <p className="field__hint" id="birth-city-hint">
          {loading
            ? 'Buscando coincidencias…'
            : 'Elige una sugerencia para fijar coordenadas y zona horaria. Si no eliges, buscamos la ciudad al enviar.'}
        </p>
        <p className="field__error" id="birth-city-error">
          Escribe la ciudad y el país, por ejemplo «Santo Domingo, República Dominicana».
        </p>
      </div>
    </>
  )
}
