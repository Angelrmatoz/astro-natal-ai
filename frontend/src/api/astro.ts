import { apiRequest } from './client'
import type {
  ChartRequest,
  ChartResponse,
  GeocodingSuggestion,
  ReadingResponse,
  TransitResponse,
} from './types'

export function saveChart(request: ChartRequest) {
  return apiRequest<ChartResponse>('/api/charts', {
    method: 'POST',
    body: request,
  })
}

export function getMyChart(signal?: AbortSignal) {
  return apiRequest<ChartResponse>('/api/charts/me', { signal })
}

export function generateReading(chartId: string, type: 'natal' | 'day' | 'week' | 'month') {
  return apiRequest<ReadingResponse>('/api/readings', {
    method: 'POST',
    body: { chartId, type },
  })
}

export function getCurrentReading(
  chartId: string,
  type: 'natal' | 'day' | 'week' | 'month',
  signal?: AbortSignal,
) {
  return apiRequest<ReadingResponse>(
    `/api/readings/current?chartId=${chartId}&type=${type}`,
    { signal },
  )
}

export function getTransits(signal?: AbortSignal) {
  return apiRequest<TransitResponse>('/api/transits/current', { signal })
}

export function searchCities(query: string, signal?: AbortSignal) {
  return apiRequest<GeocodingSuggestion[]>(
    `/api/geocoding/search?q=${encodeURIComponent(query)}`,
    { signal },
  )
}
