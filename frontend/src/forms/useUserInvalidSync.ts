import { useEffect, useRef } from 'react'

function matchesUserInvalid(element: HTMLInputElement) {
  try {
    return element.matches(':user-invalid')
  } catch {
    // jsdom y navegadores antiguos no reconocen :user-invalid.
    return false
  }
}

export function useUserInvalidSync<T extends HTMLElement = HTMLElement>() {
  const ref = useRef<T>(null)

  useEffect(() => {
    const root = ref.current
    if (!root) {
      return
    }

    const sync = (target: EventTarget | null) => {
      if (!(target instanceof HTMLInputElement)) {
        return
      }
      target.setAttribute('aria-invalid', matchesUserInvalid(target) ? 'true' : 'false')
    }

    root.querySelectorAll<HTMLInputElement>('input').forEach((field) => {
      if (!field.hasAttribute('aria-invalid')) {
        field.setAttribute('aria-invalid', 'false')
      }
    })

    const onBlur = (event: Event) => sync(event.target)
    const onInput = (event: Event) => sync(event.target)
    const onInvalid = (event: Event) => sync(event.target)

    root.addEventListener('blur', onBlur, true)
    root.addEventListener('input', onInput)
    root.addEventListener('invalid', onInvalid, true)
    return () => {
      root.removeEventListener('blur', onBlur, true)
      root.removeEventListener('input', onInput)
      root.removeEventListener('invalid', onInvalid, true)
    }
  }, [])

  return ref
}
