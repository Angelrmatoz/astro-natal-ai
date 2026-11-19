import { apiRequest } from './client'
import type { ChartRequest, ChartResponse, ReadingResponse, TransitResponse } from './types'

export function saveChart(token: string, request: ChartRequest) {
  return apiRequest<ChartResponse>('/api/charts', {
    method: 'POST',
    body: request,
    token,
  })
}

export function getMyChart(token: string, signal?: AbortSignal) {
  return apiRequest<ChartResponse>('/api/charts/me', { token, signal })
}

export function generateReading(token: string, chartId: string, type: 'natal' | 'day' | 'week' | 'month') {
  return apiRequest<ReadingResponse>('/api/readings', {
    method: 'POST',
    body: { chartId, type },
    token,
  })
}

export function getTransits(token: string, signal?: AbortSignal) {
  return apiRequest<TransitResponse>('/api/transits/current', { token, signal })
}
