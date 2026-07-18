import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Speedometer from '../components/Speedometer'
import { useExamSession } from '../hooks/useExamSession'
import type { Direction } from '../types'

/**
 * Exam mode (US-2.1). This page SIMULATES driving inputs and streams them as
 * raw telemetry. Look closely at what it never does: compute a score, decide
 * an infraction, or claim a result. The score panel is a pure display of what
 * the backend said.
 */
export default function ExamPage() {
  const navigate = useNavigate()
  const { exam, notice, start, sendSignal, sendTurn, sendSpeed } = useExamSession()

  const [speed, setSpeed] = useState(0)
  const [signals, setSignals] = useState<{ LEFT: boolean; RIGHT: boolean }>({
    LEFT: false,
    RIGHT: false,
  })
  const speedRef = useRef(speed)
  speedRef.current = speed

  const active = exam?.status === 'IN_PROGRESS'

  // Stream a speed reading once per second while the exam runs — the pace of
  // a real dashboard, and slow enough that honest driving stays plausible.
  useEffect(() => {
    if (!active) return
    const id = setInterval(() => {
      void sendSpeed(speedRef.current)
    }, 1000)
    return () => clearInterval(id)
  }, [active, sendSpeed])

  useEffect(() => {
    if (exam && exam.status !== 'IN_PROGRESS') {
      navigate(`/results/${exam.sessionId}`)
    }
  }, [exam, navigate])

  const toggleSignal = useCallback(
    async (direction: Direction) => {
      const next = !signals[direction]
      // A real stalk switch: engaging one signal releases the other.
      setSignals({ LEFT: false, RIGHT: false, [direction]: next } as typeof signals)
      await sendSignal(next, direction)
    },
    [signals, sendSignal],
  )

  const accelerate = (delta: number) =>
    setSpeed((s) => Math.max(0, Math.min(160, s + delta)))

  if (!exam) {
    return (
      <div className="card">
        <h1>Exam Mode</h1>
        <p className="hint">
          You will be graded server-side: turn signals before turns, respect the speed limit.
          Three critical mistakes end the exam immediately.
        </p>
        <button className="btn btn-primary" onClick={() => void start()}>
          Start exam
        </button>
      </div>
    )
  }

  return (
    <div className="exam-layout">
      <section className="card">
        <h2>Vehicle</h2>
        <Speedometer speed={speed} limit={exam.speedLimitKmh} />
        <div className="pedals">
          <button className="btn" onClick={() => accelerate(-10)} disabled={!active}>
            🦶 Brake (−10)
          </button>
          <button className="btn" onClick={() => accelerate(5)} disabled={!active}>
            ⛽ Accelerate (+5)
          </button>
        </div>
        <div className="signals">
          <button
            className={`btn ${signals.LEFT ? 'btn-active' : ''}`}
            onClick={() => void toggleSignal('LEFT')}
            disabled={!active}
          >
            ⬅️ Left signal {signals.LEFT ? 'ON' : 'OFF'}
          </button>
          <button
            className={`btn ${signals.RIGHT ? 'btn-active' : ''}`}
            onClick={() => void toggleSignal('RIGHT')}
            disabled={!active}
          >
            Right signal {signals.RIGHT ? 'ON' : 'OFF'} ➡️
          </button>
        </div>
        <div className="turns">
          <button className="btn" onClick={() => void sendTurn('LEFT')} disabled={!active}>
            ↰ Turn left
          </button>
          <button className="btn" onClick={() => void sendTurn('RIGHT')} disabled={!active}>
            Turn right ↱
          </button>
        </div>
        <div className="cheat-corner">
          <button
            className="btn btn-danger"
            disabled={!active}
            onClick={() => {
              // Deliberate tamper demo: teleport the speedometer. The server's
              // plausibility check (REQ-SEC-03) rejects it and penalizes us.
              setSpeed(160)
              void sendSpeed(160)
            }}
          >
            🏴‍☠️ Try to cheat: teleport to 160 km/h
          </button>
        </div>
      </section>

      <section className="card">
        <h2>Examiner (server-side)</h2>
        <dl className="scoreboard">
          <dt>Status</dt>
          <dd>{exam.status}</dd>
          <dt>Score</dt>
          <dd>{exam.currentScore}</dd>
          <dt>Infractions</dt>
          <dd>
            {exam.totalInfractionCount} ({exam.criticalInfractionCount} critical)
          </dd>
        </dl>
        {notice && <p className="notice">{notice}</p>}
        <button
          className="btn btn-primary"
          onClick={() => navigate(`/results/${exam.sessionId}`)}
          disabled={!active}
        >
          Finish exam
        </button>
      </section>
    </div>
  )
}
