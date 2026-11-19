import type { ChartPoint } from '../api/types'

export function formatPosition(point: ChartPoint | null | undefined) {
  if (!point) {
    return 'Sin hora'
  }
  const degrees = Math.floor(point.degree)
  const rawMinutes = Math.round((point.degree - degrees) * 60)
  const minutes = rawMinutes === 60 ? 0 : rawMinutes
  const safeDegrees = rawMinutes === 60 ? degrees + 1 : degrees
  return `${safeDegrees}° ${String(minutes).padStart(2, '0')}′ ${point.sign}`
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat('es', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(new Date(`${value}T00:00:00Z`))
}

export function formatTime(value: string | null) {
  return value ? value.slice(0, 5) : 'hora desconocida'
}
