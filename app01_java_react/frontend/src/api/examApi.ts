import { http } from './httpClient'
import type { ExamEvent, ExamResult, ExamState } from '../types'

export async function startExam(): Promise<ExamState> {
  const { data } = await http.post<ExamState>('/exam/start')
  return data
}

export async function sendEvent(sessionId: number, event: ExamEvent): Promise<ExamState> {
  const { data } = await http.post<ExamState>(`/exam/${sessionId}/events`, event)
  return data
}

export async function getExamState(sessionId: number): Promise<ExamState> {
  const { data } = await http.get<ExamState>(`/exam/${sessionId}/state`)
  return data
}

// Note: no body. The backend decides the outcome; we only ask it to conclude.
export async function finishExam(sessionId: number): Promise<ExamResult> {
  const { data } = await http.post<ExamResult>(`/exam/${sessionId}/finish`)
  return data
}
