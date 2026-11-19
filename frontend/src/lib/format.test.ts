import { formatDate, formatPosition, formatTime } from './format'

describe('formatPosition', () => {
  it('converts decimal degrees to degrees and minutes', () => {
    expect(formatPosition({ sign: 'Virgo', degree: 21.24, longitude: 171.24 })).toBe(
      '21° 14′ Virgo',
    )
  })

  it('rounds 59.6 minutes up to the next degree', () => {
    expect(formatPosition({ sign: 'Aries', degree: 4.999, longitude: 4.999 })).toBe('5° 00′ Aries')
  })

  it('marks a missing point as unknown time', () => {
    expect(formatPosition(null)).toBe('Sin hora')
  })
})

describe('formatDate', () => {
  it('formats an ISO date in Spanish without shifting the day', () => {
    expect(formatDate('1985-02-10')).toContain('10')
    expect(formatDate('1985-02-10')).toContain('1985')
  })
})

describe('formatTime', () => {
  it('trims seconds from the backend time', () => {
    expect(formatTime('14:30:00')).toBe('14:30')
  })

  it('describes a missing time', () => {
    expect(formatTime(null)).toBe('hora desconocida')
  })
})
