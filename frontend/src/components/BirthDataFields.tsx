import { useState } from 'react'

type BirthDataFieldsProps = {
  initial?: {
    fechaNac: string
    horaNac: string | null
    timeKnown: boolean
    ciudad: string
  }
  fechaMax?: string
}

export function BirthDataFields({ initial, fechaMax }: BirthDataFieldsProps) {
  const [unknownTime, setUnknownTime] = useState(initial ? !initial.timeKnown : false)

  return (
    <>
      <input type="hidden" name="timeKnown" value={unknownTime ? 'false' : 'true'} />

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
          defaultValue={initial?.ciudad ?? ''}
          required
          maxLength={160}
          autoComplete="address-level2"
          placeholder="Santo Domingo, República Dominicana"
          aria-errormessage="birth-city-error"
        />
        <p className="field__hint">Buscamos las coordenadas y la zona horaria con Open-Meteo.</p>
        <p className="field__error" id="birth-city-error">
          Escribe la ciudad y el país, por ejemplo «Santo Domingo, República Dominicana».
        </p>
      </div>
    </>
  )
}
