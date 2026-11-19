import { ApiError } from '../api/client'

export function chartErrorMessage(cause: unknown): string {
  if (cause instanceof ApiError) {
    if (cause.status === 422) {
      return 'No encontramos esa ciudad. Prueba con el formato «Ciudad, País».'
    }
    if (cause.status === 503) {
      return 'El motor astrológico no está disponible ahora. Intenta de nuevo en unos minutos.'
    }
    return cause.message
  }
  return 'No se pudo calcular tu carta. Intenta de nuevo.'
}
