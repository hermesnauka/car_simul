import { http } from './httpClient'
import type { AuthResponse } from '../types'

export async function login(username: string, password: string): Promise<AuthResponse> {
  const { data } = await http.post<AuthResponse>('/auth/login', { username, password })
  return data
}

export async function register(username: string, password: string): Promise<AuthResponse> {
  const { data } = await http.post<AuthResponse>('/auth/register', { username, password })
  return data
}
