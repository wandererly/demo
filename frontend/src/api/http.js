import { clearAuth, getStoredToken } from '../auth'

export const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

async function request(path, options = {}) {
  const token = getStoredToken()
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    },
    ...options
  })
  if (!res.ok) {
    let msg = `HTTP ${res.status}: ${res.statusText}`
    try {
      const body = await res.json()
      if (body && body.message) msg = body.message
    } catch (_) { /* ignore non-JSON error body */ }
    if (res.status === 401 || res.status === 403) {
      clearAuth()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      msg = '登录已过期，请重新登录'
    }
    throw new Error(msg)
  }
  const data = await res.json()
  if (data && data.code && data.code !== 0) {
    throw new Error(data.message || '请求失败')
  }
  return data.data
}

export const http = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) }),
  put: (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) }),
  del: (path) => request(path, { method: 'DELETE' })
}
