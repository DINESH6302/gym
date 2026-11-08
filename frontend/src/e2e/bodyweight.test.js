// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useStore, DEF } from '../store/useStore.js'

const clone = o => JSON.parse(JSON.stringify(o))

vi.mock('../lib/api.js', () => ({
  api: vi.fn(() => Promise.resolve({})),
  getStoredToken: () => 'test-token',
  setStoredToken: vi.fn(),
  clearToken: vi.fn(),
}))
vi.mock('../lib/api-client.js', () => ({
  addBodyweight: vi.fn(() => Promise.resolve({})),
  deleteBodyweight: vi.fn(() => Promise.resolve({})),
  updateSettings: vi.fn(() => Promise.resolve({})),
  createRoutine: vi.fn(() => Promise.resolve({})),
  updateRoutine: vi.fn(() => Promise.resolve({})),
  deleteRoutine: vi.fn(() => Promise.resolve({})),
  updateWeekSchedule: vi.fn(() => Promise.resolve({})),
  setExerciseWeight: vi.fn(() => Promise.resolve({})),
}))

beforeEach(() => {
  useStore.setState({ S: clone(DEF), user: { id: 'u1', name: 'Test' }, ready: true })
})
afterEach(() => { vi.clearAllMocks() })

describe('Bodyweight: Logging', () => {
  it('starts with empty bodyweight array', () => {
    expect(useStore.getState().S.bodyweight).toEqual([])
  })

  it('adds a bodyweight entry', async () => {
    await useStore.getState().addBodyweight({ d: '2026-11-08', w: 73, t: Date.now() })
    expect(useStore.getState().S.bodyweight).toHaveLength(1)
    expect(useStore.getState().S.bodyweight[0].w).toBe(73)
  })

  it('adds multiple entries on different days', async () => {
    await useStore.getState().addBodyweight({ d: '2026-11-07', w: 73.5, t: 1000 })
    await useStore.getState().addBodyweight({ d: '2026-11-08', w: 73.0, t: 2000 })
    await useStore.getState().addBodyweight({ d: '2026-11-09', w: 72.8, t: 3000 })

    expect(useStore.getState().S.bodyweight).toHaveLength(3)
  })

  it('removes a bodyweight entry by index', async () => {
    await useStore.getState().addBodyweight({ d: '2026-11-07', w: 73, t: 1000 })
    await useStore.getState().addBodyweight({ d: '2026-11-08', w: 74, t: 2000 })

    await useStore.getState().removeBodyweight(0)

    expect(useStore.getState().S.bodyweight).toHaveLength(1)
    expect(useStore.getState().S.bodyweight[0].w).toBe(74)
  })

  it('handles decimal weights', async () => {
    await useStore.getState().addBodyweight({ d: '2026-11-08', w: 73.25, t: Date.now() })
    expect(useStore.getState().S.bodyweight[0].w).toBe(73.25)
  })

  it('supports very low bodyweight (child/lightweight)', async () => {
    await useStore.getState().addBodyweight({ d: '2026-11-08', w: 35, t: Date.now() })
    expect(useStore.getState().S.bodyweight[0].w).toBe(35)
  })

  it('supports high bodyweight', async () => {
    await useStore.getState().addBodyweight({ d: '2026-11-08', w: 150, t: Date.now() })
    expect(useStore.getState().S.bodyweight[0].w).toBe(150)
  })
})

describe('Bodyweight: Target weight', () => {
  it('sets a target weight', () => {
    useStore.getState().update(s => { s.targetW = 70 }, false)
    expect(useStore.getState().S.targetW).toBe(70)
  })

  it('clears target weight', () => {
    useStore.getState().update(s => { s.targetW = 70 }, false)
    useStore.getState().update(s => { s.targetW = null }, false)
    expect(useStore.getState().S.targetW).toBeNull()
  })
})
