import { useId, useState } from 'react'

type PasswordFieldProps = {
  name: string
  label: string
  autoComplete: string
  hint?: string
  error: string
  minLength?: number
}

export function PasswordField({
  name,
  label,
  autoComplete,
  hint,
  error,
  minLength,
}: PasswordFieldProps) {
  const id = useId()
  const hintId = `${id}-hint`
  const errorId = `${id}-error`
  const [visible, setVisible] = useState(false)

  return (
    <div className="field">
      <div className="field__head">
        <label htmlFor={id}>{label}</label>
        <button
          type="button"
          className="field__toggle"
          aria-pressed={visible}
          onClick={() => setVisible((current) => !current)}
        >
          {visible ? 'Ocultar' : 'Mostrar'}
        </button>
      </div>
      {hint ? (
        <p className="field__hint" id={hintId}>
          {hint}
        </p>
      ) : null}
      <input
        id={id}
        name={name}
        type={visible ? 'text' : 'password'}
        autoComplete={autoComplete}
        required
        minLength={minLength}
        maxLength={72}
        spellCheck={false}
        enterKeyHint="done"
        aria-describedby={hint ? hintId : undefined}
        aria-errormessage={errorId}
      />
      <p className="field__error" id={errorId}>
        {error}
      </p>
    </div>
  )
}
