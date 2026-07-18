import { useCallback, useRef, useState } from 'react'
import axios from 'axios'
import * as examApi from '../api/examApi'
import type { Direction, ExamEvent, ExamState } from '../types'

/**
 * Encapsulates the exam telemetry stream. Local state here (signal toggles,
 * simulated speed) is UI input only — every judgement about it comes back
 * from the server in ExamState. The UI never decides anything.
 */
export function useExamSession() {
  const [exam, setExam] = useState<ExamState | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const sessionIdRef = useRef<number | null>(null)

  const start = useCallback(async () => {
    const state = await examApi.startExam()
    sessionIdRef.current = state.sessionId
    setExam(state)
    setNotice(null)
    return state
  }, [])

  const send = useCallback(async (event: ExamEvent): Promise<ExamState | null> => {
    const sessionId = sessionIdRef.current
    if (sessionId === null) return null
    try {
      const state = await examApi.sendEvent(sessionId, event)
      setExam(state)
      if (state.lastInfraction) {
        setNotice(`${state.lastInfraction.severity}: ${state.lastInfraction.message}`)
      }
      return state
    } catch (err) {
      if (axios.isAxiosError(err) && err.response) {
        const { status, data } = err.response
        if (status === 422) {
          // Implausible telemetry: the server rejected it AND penalized us.
          const state = await examApi.getExamState(sessionId)
          setExam(state)
          setNotice(`REJECTED BY SERVER: ${data?.message ?? 'implausible telemetry'}`)
          return state
        }
        if (status === 409) {
          // Exam already terminated server-side (e.g. auto-fail).
          const state = await examApi.getExamState(sessionId)
          setExam(state)
          return state
        }
      }
      setNotice('Failed to send telemetry')
      return null
    }
  }, [])

  const sendSignal = useCallback(
    (on: boolean, direction: Direction) =>
      send({
        type: on ? 'TURN_SIGNAL_ON' : 'TURN_SIGNAL_OFF',
        direction,
        clientTimestamp: Date.now(),
      }),
    [send],
  )

  const sendTurn = useCallback(
    (direction: Direction) => send({ type: 'TURN', direction, clientTimestamp: Date.now() }),
    [send],
  )

  const sendSpeed = useCallback(
    (speedKmh: number) => send({ type: 'SPEED_UPDATE', speedKmh, clientTimestamp: Date.now() }),
    [send],
  )

  return { exam, notice, start, sendSignal, sendTurn, sendSpeed }
}
