import type { ChartRequest } from '../api/types'

export function chartRequestFromFormData(data: FormData): ChartRequest {
  const timeKnown = data.get('timeKnown') === 'true'
  const latitud = String(data.get('latitud') ?? '').trim()
  const longitud = String(data.get('longitud') ?? '').trim()
  const timezone = String(data.get('timezone') ?? '').trim()

  const request: ChartRequest = {
    fechaNac: String(data.get('fechaNac') ?? ''),
    horaNac: timeKnown ? String(data.get('horaNac') ?? '') : null,
    timeKnown,
    ciudad: String(data.get('ciudad') ?? '').trim(),
  }

  if (latitud !== '' && longitud !== '' && timezone !== '') {
    request.latitud = Number(latitud)
    request.longitud = Number(longitud)
    request.timezone = timezone
  }

  return request
}
