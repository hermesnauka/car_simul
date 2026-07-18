import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { getToken, setToken } from '../api/httpClient'
import * as authApi from '../api/authApi'

interface AuthContextValue {
  username: string | null
  isAuthenticated: boolean
  login: (username: string, password: string) => Promise<void>
  register: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

const USERNAME_KEY = 'carsimul.username'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(() =>
    getToken() ? sessionStorage.getItem(USERNAME_KEY) : null,
  )

  const handleAuth = useCallback((token: string, name: string) => {
    setToken(token)
    sessionStorage.setItem(USERNAME_KEY, name)
    setUsername(name)
  }, [])

  const login = useCallback(async (name: string, password: string) => {
    const res = await authApi.login(name, password)
    handleAuth(res.token, res.username)
  }, [handleAuth])

  const register = useCallback(async (name: string, password: string) => {
    const res = await authApi.register(name, password)
    handleAuth(res.token, res.username)
  }, [handleAuth])

  const logout = useCallback(() => {
    setToken(null)
    sessionStorage.removeItem(USERNAME_KEY)
    setUsername(null)
  }, [])

  return (
    <AuthContext.Provider value={{ username, isAuthenticated: username !== null, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
