const CENTER = 200

function polar(radius: number, degrees: number) {
  const radians = ((degrees - 90) * Math.PI) / 180
  return [CENTER + radius * Math.cos(radians), CENTER + radius * Math.sin(radians)]
}

function arc(radius: number, start: number, end: number) {
  const [x1, y1] = polar(radius, start)
  const [x2, y2] = polar(radius, end)
  const largeArc = end - start > 180 ? 1 : 0
  return `M ${x1.toFixed(2)} ${y1.toFixed(2)} A ${radius} ${radius} 0 ${largeArc} 1 ${x2.toFixed(2)} ${y2.toFixed(2)}`
}

const TICKS = Array.from({ length: 72 }, (_, index) => index * 5)
const DOTS = Array.from({ length: 12 }, (_, index) => index * 30)

export function EclipticDial() {
  return (
    <svg className="dial" viewBox="0 0 400 400" aria-hidden="true" focusable="false">
      <circle className="dial__ring dial__ring--outer" cx={CENTER} cy={CENTER} r="188" />
      <circle className="dial__ring" cx={CENTER} cy={CENTER} r="150" />
      <circle className="dial__ring dial__ring--inner" cx={CENTER} cy={CENTER} r="112" />

      <g className="dial__ticks">
        {TICKS.map((degrees) => {
          const major = degrees % 30 === 0
          const [x1, y1] = polar(major ? 168 : 178, degrees)
          const [x2, y2] = polar(188, degrees)
          return (
            <line
              key={degrees}
              className={major ? 'dial__tick dial__tick--major' : 'dial__tick'}
              x1={x1.toFixed(2)}
              y1={y1.toFixed(2)}
              x2={x2.toFixed(2)}
              y2={y2.toFixed(2)}
            />
          )
        })}
      </g>

      <g className="dial__dots">
        {DOTS.map((degrees) => {
          const [cx, cy] = polar(131, degrees)
          return <circle key={degrees} cx={cx.toFixed(2)} cy={cy.toFixed(2)} r="2.5" />
        })}
      </g>

      <path className="dial__arc" d={arc(160, 0, 118)} />
      <circle className="dial__center" cx={CENTER} cy={CENTER} r="3" />
    </svg>
  )
}
