const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  token?: string | null
  signal?: AbortSignal
}

type ProblemDetail = {
  title?: string
  detail?: string
}

export const UNAUTHORIZED_EVENT = 'auth:unauthorized'

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  if (options.token) {
    headers.Authorization = `Bearer ${options.token}`
  }

  let response: Response
  try {
    response = await fetch(`${API_URL}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      signal: options.signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw error
    }
    throw new ApiError(0, 'No se pudo conectar con el servidor. Revisa tu conexión.')
  }

  if (!response.ok) {
    if (response.status === 401 && options.token) {
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
    }
    const problem = await response
      .json()
      .then((value: ProblemDetail) => value)
      .catch(() => null)
    throw new ApiError(
      response.status,
      problem?.detail ?? problem?.title ?? `Error ${response.status}`,
    )
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}
