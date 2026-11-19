import type { ChartRequest } from '../api/types'

export function chartRequestFromFormData(data: FormData): ChartRequest {
  const timeKnown = data.get('timeKnown') === 'true'
  return {
    fechaNac: String(data.get('fechaNac') ?? ''),
    horaNac: timeKnown ? String(data.get('horaNac') ?? '') : null,
    timeKnown,
    ciudad: String(data.get('ciudad') ?? '').trim(),
  }
}
