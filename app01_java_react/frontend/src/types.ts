// TypeScript mirrors of the backend DTO contract. Keeping these explicit makes
// the frontend/backend boundary auditable: note there is no field anywhere for
// the client to claim a score or a pass/fail outcome.

export interface AuthResponse {
  token: string
  username: string
  role: string
}

export interface CarState {
  hazardLights: boolean
  acOn: boolean
  headlights: boolean
  leftSignal: boolean
  rightSignal: boolean
}

export type Direction = 'LEFT' | 'RIGHT'

export type ExamEvent =
  | { type: 'TURN_SIGNAL_ON'; direction: Direction; clientTimestamp: number }
  | { type: 'TURN_SIGNAL_OFF'; direction: Direction; clientTimestamp: number }
  | { type: 'TURN'; direction: Direction; clientTimestamp: number }
  | { type: 'SPEED_UPDATE'; speedKmh: number; clientTimestamp: number }

export interface InfractionInfo {
  ruleCode: string
  severity: 'MINOR' | 'CRITICAL'
  message: string
  occurredAt: string
}

export interface ExamState {
  sessionId: number
  status: 'IN_PROGRESS' | 'PASSED' | 'FAILED'
  currentScore: number
  criticalInfractionCount: number
  totalInfractionCount: number
  speedLimitKmh: number
  startedAt: string
  lastInfraction: InfractionInfo | null
}

export interface ExamResult {
  sessionId: number
  status: 'PASSED' | 'FAILED'
  finalScore: number
  criticalInfractionCount: number
  totalInfractionCount: number
  infractions: InfractionInfo[]
  startedAt: string
  endedAt: string
}
