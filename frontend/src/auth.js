export const TOKEN_KEY = 'hrm_token'
export const USER_KEY = 'hrm_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token, remember) {
  if (remember) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    sessionStorage.setItem(TOKEN_KEY, token)
  }
}

export function setUser(user, remember) {
  const raw = JSON.stringify(user)
  if (remember) {
    localStorage.setItem(USER_KEY, raw)
  } else {
    sessionStorage.setItem(USER_KEY, raw)
  }
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function getPermissions() {
  const user = getUser()
  return user && user.permissions ? user.permissions : []
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
}

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || ''
}
