interface Props {
  speed: number
  limit: number
}

const MAX_SPEED = 160

/** Simple SVG gauge — the 3D cockpit comes in a later milestone. */
export default function Speedometer({ speed, limit }: Props) {
  // Sweep from -120° (0 km/h) to +120° (MAX_SPEED).
  const angle = -120 + (Math.min(speed, MAX_SPEED) / MAX_SPEED) * 240
  const speeding = speed > limit

  return (
    <svg viewBox="0 0 200 140" className="speedometer" role="img" aria-label={`${speed} km/h`}>
      <path d="M 30 120 A 80 80 0 1 1 170 120" fill="none" stroke="#333" strokeWidth="12" />
      <line
        x1="100"
        y1="110"
        x2="100"
        y2="40"
        stroke={speeding ? '#ff4d4d' : '#4dff88'}
        strokeWidth="4"
        strokeLinecap="round"
        transform={`rotate(${angle} 100 110)`}
      />
      <circle cx="100" cy="110" r="8" fill="#666" />
      <text x="100" y="135" textAnchor="middle" className="speed-text">
        {Math.round(speed)} km/h
      </text>
      <text x="100" y="30" textAnchor="middle" className="limit-text">
        limit {limit}
      </text>
    </svg>
  )
}
