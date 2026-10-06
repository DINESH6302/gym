// Backend API helpers — JWT auth.
export const IS_APPLE = /iPhone|iPad|iPod|Macintosh/.test(navigator.userAgent)
export const IS_ANDROID = /Android/.test(navigator.userAgent)

const TOKEN_KEY = 'gym_token'

let remoteBase = ''
let remoteToken = null
export function setRemoteAuth(base, token) { remoteBase = base || ''; remoteToken = token || null }

export function appBase(loc = typeof location !== 'undefined' ? location : null) {
  const path = (loc && loc.pathname) || '/'
  return path.slice(0, path.lastIndexOf('/') + 1) || '/'
}

export function getStoredToken() { return localStorage.getItem(TOKEN_KEY) || '' }
export function setStoredToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}
export function clearToken() { setStoredToken('') }

export async function api(path, opts) {
  const headers = Object.assign({ 'Content-Type': 'application/json' }, opts && opts.headers)
  const token = remoteToken || getStoredToken()
  if (token) headers.Authorization = 'Bearer ' + token
  const url = remoteBase ? remoteBase + path : appBase().replace(/\/$/, '') + path
  const r = await fetch(url, Object.assign({}, opts, { headers }))
  const data = await r.json().catch(() => ({}))
  if (!r.ok) { const e = new Error(data.error || ('HTTP ' + r.status)); e.status = r.status; e.data = data; throw e }
  return data
}

/** Create a new account. Stores JWT automatically. */
export async function registerUser(name, email, password) {
  const res = await api('/api/auth/register', {
    method: 'POST', body: JSON.stringify({ name, email, password })
  })
  if (res.token) setStoredToken(res.token)
  return res
}

/** Login with email + password. Stores JWT automatically. */
export async function loginUser(email, password) {
  const res = await api('/api/auth/login', {
    method: 'POST', body: JSON.stringify({ email, password })
  })
  if (res.token) setStoredToken(res.token)
  return res
}

/** Logout — clear stored JWT. */
export function logout() {
  clearToken()
}

export async function pairRedeem(serverBase, code) {
  const r = await fetch(serverBase + '/api/pair/redeem', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ code })
  })
  const data = await r.json().catch(() => ({}))
  if (!r.ok) { const e = new Error(data.error || ('HTTP ' + r.status)); e.status = r.status; throw e }
  return data
}
