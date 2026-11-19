import type { EnginePlanet } from '../api/types'
import { formatPosition } from '../lib/format'

export function PlanetTable({ planets }: { planets: EnginePlanet[] }) {
  return (
    <table className="table">
      <thead>
        <tr>
          <th scope="col">Planeta</th>
          <th scope="col">Posición</th>
          <th scope="col">Movimiento</th>
        </tr>
      </thead>
      <tbody>
        {planets.map((planet) => (
          <tr key={planet.name}>
            <th scope="row">{planet.name}</th>
            <td>{formatPosition(planet)}</td>
            <td>{planet.retrograde ? 'Retrógrado' : 'Directo'}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
