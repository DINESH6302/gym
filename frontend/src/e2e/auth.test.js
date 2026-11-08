// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useStore, DEF } from '../store/useStore.js'

const clone = o => JSON.parse(JSON.stringify(o))

// Mock fetch for API calls
let fetchMock
beforeEach(() => {
  fetchMock = vi.fn()
  globalThis.fetch = fetchMock
  localStorage.clear()
  useStore.setState({ S: clone(DEF), user: null, ready: false })
})
afterEach(() => { vi.restoreAllMocks() })

describe('Auth: Registration', () => {
  it('registers a new user and stores JWT token', async () => {
    const { registerUser, getStoredToken } = await import('../lib/api.js')
    fetchMock.mockResolvedValueOnce({
      ok: true, json: async () => ({ token: 'jwt-token-123', user: { id: 'u1', name: 'Test', email: 'test@test.com' } })
    })

    const res = await registerUser('Test', 'test@test.com', 'password123')
    expect(res.token).toBe('jwt-token-123')
    expect(getStoredToken()).toBe('jwt-token-123')
  })

  it('rejects registration with duplicate email', async () => {
    const { registerUser } = await import('../lib/api.js')
    fetchMock.mockResolvedValueOnce({
      ok: false, status: 400, json: async () => ({ error: 'Email already registered' })
    })

    await expect(registerUser('Test', 'dupe@test.com', 'pass')).rejects.toMatchObject({
      message: 'Email already registered', status: 400
    })
  })

  it('rejects registration with empty fields', async () => {
    const { registerUser } = await import('../lib/api.js')
    fetchMock.mockResolvedValueOnce({
      ok: false, status: 400, json: async () => ({ error: 'name is required' })
    })

    await expect(registerUser('', '', '')).rejects.toMatchObject({ status: 400 })
  })
})

describe('Auth: Login', () => {
  it('logs in with correct credentials and stores JWT', async () => {
    const { loginUser, getStoredToken } = await import('../lib/api.js')
    fetchMock.mockResolvedValueOnce({
      ok: true, json: async () => ({ token: 'jwt-login-456' })
    })

    const res = await loginUser('test@test.com', 'password123')
    expect(res.token).toBe('jwt-login-456')
    expect(getStoredToken()).toBe('jwt-login-456')
  })

  it('rejects login with wrong password', async () => {
    const { loginUser } = await import('../lib/api.js')
    fetchMock.mockResolvedValueOnce({
      ok: false, status: 401, json: async () => ({ error: 'Invalid credentials' })
    })

    await expect(loginUser('test@test.com', 'wrong')).rejects.toMatchObject({ status: 401 })
  })

  it('rejects login with non-existent email', async () => {
    const { loginUser } = await import('../lib/api.js')
    fetchMock.mockResolvedValueOnce({
      ok: false, status: 401, json: async () => ({ error: 'Invalid credentials' })
    })

    await expect(loginUser('nobody@test.com', 'pass')).rejects.toMatchObject({ status: 401 })
  })
})

describe('Auth: Logout', () => {
  it('clears token on logout', async () => {
    const { setStoredToken, logout, getStoredToken } = await import('../lib/api.js')
    setStoredToken('some-token')
    expect(getStoredToken()).toBe('some-token')

    logout()
    expect(getStoredToken()).toBe('')
  })
})

describe('Auth: JWT token in requests', () => {
  it('attaches Bearer token to API calls', async () => {
    const { api, setStoredToken } = await import('../lib/api.js')
    setStoredToken('my-jwt')
    fetchMock.mockResolvedValueOnce({ ok: true, json: async () => ({ data: 'ok' }) })

    await api('/api/test')

    const [url, opts] = fetchMock.mock.calls[0]
    expect(opts.headers.Authorization).toBe('Bearer my-jwt')
  })

  it('does not attach token when none is stored', async () => {
    const { api, clearToken } = await import('../lib/api.js')
    clearToken()
    fetchMock.mockResolvedValueOnce({ ok: true, json: async () => ({}) })

    await api('/api/test')

    const [url, opts] = fetchMock.mock.calls[0]
    expect(opts.headers.Authorization).toBeUndefined()
  })
})

describe('Auth: Boot flow', () => {
  it('shows login when no token exists', async () => {
    localStorage.removeItem('gym_token')
    await useStore.getState().boot()
    expect(useStore.getState().ready).toBe(true)
    expect(useStore.getState().user).toBeNull()
  })

  it('boots with valid token and loads user data', async () => {
    localStorage.setItem('gym_token', 'valid-jwt')
    fetchMock
      .mockResolvedValueOnce({ ok: true, json: async () => ({ user: { id: 'u1', name: 'Test', email: 'test@test.com' } }) }) // /api/me
      .mockResolvedValue({ ok: true, json: async () => ({}) }) // all other endpoints

    await useStore.getState().boot()
    expect(useStore.getState().user).toEqual({ id: 'u1', name: 'Test', email: 'test@test.com' })
    expect(useStore.getState().ready).toBe(true)
  })

  it('clears token and shows login on 401', async () => {
    localStorage.setItem('gym_token', 'expired-jwt')
    fetchMock.mockResolvedValueOnce({
      ok: false, status: 401, json: async () => ({ error: 'token expired' })
    })

    await useStore.getState().boot()
    expect(useStore.getState().user).toBeNull()
    expect(useStore.getState().ready).toBe(true)
    expect(localStorage.getItem('gym_token')).toBeNull()
  })
})
