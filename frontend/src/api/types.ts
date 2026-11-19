export type SessionUser = {
  id: string
  email: string
  nombre: string
}

export type AuthResponse = {
  token: string
  tokenType: string
  expiresAt: string
  user: SessionUser
}

export type ChartPoint = {
  sign: string
  degree: number
  longitude: number
}

export type EnginePlanet = ChartPoint & {
  name: string
  retrograde: boolean
}

export type EngineAspect = {
  a: string
  b: string
  type: string
  orb: number
}

export type EngineChart = {
  meta: {
    fecha: string
    hora: string | null
    time_known: boolean
    lat: number
    lng: number
    timezone: string
    house_system: string
    engine: string
  }
  planets: EnginePlanet[]
  aspects: EngineAspect[]
  ascendant: ChartPoint | null
  midheaven: ChartPoint | null
  houses: { number: number; sign: string; cusp: number }[]
}

export type ChartResponse = {
  id: string
  nombre: string
  fechaNac: string
  horaNac: string | null
  timeKnown: boolean
  ciudad: string
  latitud: number
  longitud: number
  timezone: string
  datosCarta: EngineChart
  creadoEn: string
}

export type ChartRequest = {
  nombre?: string
  fechaNac: string
  horaNac: string | null
  timeKnown: boolean
  ciudad: string
}

export type ReadingResponse = {
  id: string
  chartId: string
  tipo: string
  rangoInicio: string
  rangoFin: string
  contenidoTexto: string
  modelo: string
  creadoEn: string
}

export type TransitAspect = {
  transit: string
  natal: string
  type: string
  orb: number
}

export type TransitResponse = {
  fecha: string
  planets: EnginePlanet[]
  aspects: TransitAspect[]
}
