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
  createRoutine: vi.fn(() => Promise.resolve({})),
  updateRoutine: vi.fn(() => Promise.resolve({})),
  deleteRoutine: vi.fn(() => Promise.resolve({})),
  updateWeekSchedule: vi.fn(() => Promise.resolve({})),
  updateSettings: vi.fn(() => Promise.resolve({})),
}))

beforeEach(() => {
  useStore.setState({ S: clone(DEF), user: { id: 'u1', name: 'Test' }, ready: true })
})
afterEach(() => { vi.clearAllMocks() })

describe('Routines: CRUD', () => {
  it('adds a new routine to the store', async () => {
    const routine = { id: 'r1', name: 'Push Day', emoji: '💪', ex: [] }
    await useStore.getState().addRoutine(routine)

    const routines = useStore.getState().S.routines
    expect(routines).toHaveLength(1)
    expect(routines[0].name).toBe('Push Day')
  })

  it('adds multiple routines', async () => {
    await useStore.getState().addRoutine({ id: 'r1', name: 'Push', emoji: '💪', ex: [] })
    await useStore.getState().addRoutine({ id: 'r2', name: 'Pull', emoji: '🏋️', ex: [] })
    await useStore.getState().addRoutine({ id: 'r3', name: 'Legs', emoji: '🦵', ex: [] })

    expect(useStore.getState().S.routines).toHaveLength(3)
  })

  it('removes a routine', async () => {
    await useStore.getState().addRoutine({ id: 'r1', name: 'Push', ex: [] })
    await useStore.getState().addRoutine({ id: 'r2', name: 'Pull', ex: [] })

    await useStore.getState().removeRoutine('r1')

    const routines = useStore.getState().S.routines
    expect(routines).toHaveLength(1)
    expect(routines[0].id).toBe('r2')
  })

  it('removing a routine also removes it from week schedule', async () => {
    useStore.getState().update(s => {
      s.routines = [{ id: 'r1', name: 'Push', ex: [] }, { id: 'r2', name: 'Pull', ex: [] }]
      s.week = { 1: ['r1', 'r2'], 3: ['r1'] }
    }, false)

    await useStore.getState().removeRoutine('r1')

    const week = useStore.getState().S.week
    expect(week[1]).toEqual(['r2'])
    expect(week[3]).toBeUndefined() // empty day removed
  })

  it('removes a routine that does not exist (no-op)', async () => {
    await useStore.getState().addRoutine({ id: 'r1', name: 'Push', ex: [] })
    await useStore.getState().removeRoutine('nonexistent')

    expect(useStore.getState().S.routines).toHaveLength(1)
  })
})

describe('Routines: Exercises within routines', () => {
  it('adds exercises to a routine via update', () => {
    useStore.getState().update(s => {
      s.routines = [{ id: 'r1', name: 'Push', ex: [] }]
    }, false)

    useStore.getState().update(s => {
      const r = s.routines.find(x => x.id === 'r1')
      r.ex.push({ id: 'bench-press', sets: 3, reps: 10, mode: 'reps' })
      r.ex.push({ id: 'incline-press', sets: 3, reps: 8, mode: 'reps' })
    })

    const r = useStore.getState().S.routines[0]
    expect(r.ex).toHaveLength(2)
    expect(r.ex[0].id).toBe('bench-press')
  })

  it('removes an exercise from a routine', () => {
    useStore.getState().update(s => {
      s.routines = [{
        id: 'r1', name: 'Push', ex: [
          { id: 'bench-press', sets: 3, reps: 10 },
          { id: 'ohp', sets: 3, reps: 8 },
        ]
      }]
    }, false)

    useStore.getState().update(s => {
      const r = s.routines.find(x => x.id === 'r1')
      r.ex = r.ex.filter(e => e.id !== 'bench-press')
    })

    expect(useStore.getState().S.routines[0].ex).toHaveLength(1)
    expect(useStore.getState().S.routines[0].ex[0].id).toBe('ohp')
  })

  it('reorders exercises within a routine', () => {
    useStore.getState().update(s => {
      s.routines = [{
        id: 'r1', name: 'Push', ex: [
          { id: 'bench-press', sets: 3, reps: 10 },
          { id: 'ohp', sets: 3, reps: 8 },
          { id: 'flyes', sets: 3, reps: 12 },
        ]
      }]
    }, false)

    useStore.getState().update(s => {
      const ex = s.routines[0].ex
      const [moved] = ex.splice(2, 1) // remove flyes
      ex.splice(0, 0, moved) // insert at front
    })

    const order = useStore.getState().S.routines[0].ex.map(e => e.id)
    expect(order).toEqual(['flyes', 'bench-press', 'ohp'])
  })

  it('handles routine with zero exercises', () => {
    useStore.getState().update(s => {
      s.routines = [{ id: 'r1', name: 'Empty', ex: [] }]
    }, false)

    const r = useStore.getState().S.routines[0]
    expect(r.ex).toEqual([])
    expect(r.ex.length).toBe(0)
  })
})

describe('Routines: Reorder routines', () => {
  it('moves a routine up in the list', () => {
    useStore.getState().update(s => {
      s.routines = [
        { id: 'r1', name: 'Push', ex: [] },
        { id: 'r2', name: 'Pull', ex: [] },
        { id: 'r3', name: 'Legs', ex: [] },
      ]
    }, false)

    useStore.getState().update(s => {
      const [moved] = s.routines.splice(2, 1)
      s.routines.splice(1, 0, moved)
    })

    const ids = useStore.getState().S.routines.map(r => r.id)
    expect(ids).toEqual(['r1', 'r3', 'r2'])
  })
})
