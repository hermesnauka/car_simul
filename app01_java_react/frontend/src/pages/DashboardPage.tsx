import { useEffect, useState } from 'react'
import { getCarState, setControl, type CarControl } from '../api/carApi'
import type { CarState } from '../types'

const CONTROLS: { key: CarControl; label: string; icon: string; stateKey: keyof CarState }[] = [
  { key: 'hazard', label: 'Hazard lights', icon: '⚠️', stateKey: 'hazardLights' },
  { key: 'ac', label: 'A/C', icon: '❄️', stateKey: 'acOn' },
  { key: 'headlights', label: 'Headlights', icon: '💡', stateKey: 'headlights' },
  { key: 'leftSignal', label: 'Left signal', icon: '⬅️', stateKey: 'leftSignal' },
  { key: 'rightSignal', label: 'Right signal', icon: '➡️', stateKey: 'rightSignal' },
]

/**
 * Practice mode (US-1.2): free exploration of dashboard controls.
 * Nothing here is graded — that only happens server-side in Exam Mode.
 */
export default function DashboardPage() {
  const [carState, setCarState] = useState<CarState | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getCarState().then(setCarState).catch(() => setError('Failed to load car state'))
  }, [])

  async function toggle(control: CarControl, stateKey: keyof CarState) {
    if (!carState) return
    try {
      setCarState(await setControl(control, !carState[stateKey]))
    } catch {
      setError('Failed to update control')
    }
  }

  if (error) return <p className="error">{error}</p>
  if (!carState) return <p>Loading…</p>

  return (
    <div>
      <h1>Practice Dashboard</h1>
      <p className="hint">
        Click the controls to learn the car's interior. Ready for the real thing? Switch to Exam
        Mode.
      </p>
      <div className="control-grid">
        {CONTROLS.map(({ key, label, icon, stateKey }) => (
          <button
            key={key}
            className={`control-button ${carState[stateKey] ? 'active' : ''} ${
              key === 'hazard' && carState.hazardLights ? 'blinking' : ''
            }`}
            onClick={() => toggle(key, stateKey)}
            aria-pressed={carState[stateKey]}
          >
            <span className="control-icon">{icon}</span>
            <span>{label}</span>
            <span className="control-state">{carState[stateKey] ? 'ON' : 'OFF'}</span>
          </button>
        ))}
      </div>
    </div>
  )
}
