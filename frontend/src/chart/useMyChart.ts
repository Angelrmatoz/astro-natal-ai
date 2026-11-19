import { useEffect, useState } from 'react'
import { getMyChart } from '../api/astro'
import { ApiError } from '../api/client'
import type { ChartResponse } from '../api/types'
import { useAuth } from '../auth/useAuth'

export type ChartStatus = 'loading' | 'ready' | 'missing' | 'error'

export function useMyChart() {
  const { session } = useAuth()
  const [chart, setChart] = useState<ChartResponse | null>(null)
  const [status, setStatus] = useState<ChartStatus>('loading')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!session) {
      return
    }
    const controller = new AbortController()

    getMyChart(session.token, controller.signal)
      .then((result) => {
        setChart(result)
        setStatus('ready')
      })
      .catch((cause: unknown) => {
        if (controller.signal.aborted) {
          return
        }
        if (cause instanceof ApiError && cause.status === 404) {
          setStatus('missing')
          return
        }
        setError(cause instanceof Error ? cause.message : 'No se pudo cargar tu carta')
        setStatus('error')
      })

    return () => controller.abort()
  }, [session])

  return { chart, status, error }
}
