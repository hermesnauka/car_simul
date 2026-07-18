import axios from 'axios'

// JWT lives in sessionStorage (not localStorage): it disappears when the tab
// closes, which narrows the window for token theft. The production-grade
// answer is an httpOnly cookie + CSRF token — out of scope for this MVP.
const TOKEN_KEY = 'carsimul.jwt'

export function getToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null) {
  if (token === null) {
    sessionStorage.removeItem(TOKEN_KEY)
  } else {
    sessionStorage.setItem(TOKEN_KEY, token)
  }
}

export const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      setToken(null)
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)
