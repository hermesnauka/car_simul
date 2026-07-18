import { http } from './httpClient'
import type { CarState } from '../types'

export type CarControl = 'hazard' | 'ac' | 'headlights' | 'leftSignal' | 'rightSignal'

export async function getCarState(): Promise<CarState> {
  const { data } = await http.get<CarState>('/car/state')
  return data
}

export async function setControl(control: CarControl, on: boolean): Promise<CarState> {
  const { data } = await http.post<CarState>(`/car/controls/${control}`, { on })
  return data
}
