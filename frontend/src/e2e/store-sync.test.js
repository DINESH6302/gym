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

const mocks = {
  updateSettings: vi.fn(() => Promise.resolve({})),
  createRoutine: vi.fn(() => Promise.resolve({})),
  updateRoutine: vi.fn(() => Promise.resolve({})),
  deleteRoutine: vi.fn(() => Promise.resolve({})),
  updateWeekSchedule: vi.fn(() => Promise.resolve({})),
  addBodyweight: vi.fn(() => Promise.resolve({})),
  setExerciseWeight: vi.fn(() => Promise.resolve({})),
}

vi.mock('../lib/api-client.js', () => ({
  updateSettings: (...a) => mocks.updateSettings(...a),
  createRoutine: (...a) => mocks.createRoutine(...a),
  updateRoutine: (...a) => mocks.updateRoutine(...a),
  deleteRoutine: (...a) => mocks.deleteRoutine(...a),
  updateWeekSchedule: (...a) => mocks.updateWeekSchedule(...a),
  addBodyweight: (...a) => mocks.addBodyweight(...a),
  setExerciseWeight: (...a) => mocks.setExerciseWeight(...a),
}))

beforeEach(() => {
  useStore.setState({ S: clone(DEF), user: { id: 'u1', name: 'Test' }, ready: true })
  Object.values(mocks).forEach(m => m.mockClear())
})
afterEach(() => { vi.clearAllMocks() })

describe('Store Auto-Sync: Settings', () => {
  it('syncs setting changes to backend', async () => {
    useStore.getState().update(s => { s.unit = 'lb' })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.updateSettings).toHaveBeenCalledWith(expect.objectContaining({ unit: 'lb' }))
  })

  it('does not sync when push=false', () => {
    useStore.getState().update(s => { s.unit = 'lb' }, false)
    expect(mocks.updateSettings).not.toHaveBeenCalled()
  })

  it('does not sync when user is null (not logged in)', () => {
    useStore.setState({ user: null })
    useStore.getState().update(s => { s.unit = 'lb' })
    expect(mocks.updateSettings).not.toHaveBeenCalled()
  })

  it('only syncs changed fields, not all settings', async () => {
    useStore.getState().update(s => { s.theme = 'light' })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.updateSettings).toHaveBeenCalledWith({ theme: 'light' })
  })

  it('maps targetW to targetWeight for backend', async () => {
    useStore.getState().update(s => { s.targetW = 70 })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.updateSettings).toHaveBeenCalledWith(expect.objectContaining({ targetWeight: 70 }))
  })

  it('maps wdec to weightDecimals for backend', async () => {
    useStore.getState().update(s => { s.wdec = 2 })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.updateSettings).toHaveBeenCalledWith(expect.objectContaining({ weightDecimals: 2 }))
  })

  it('maps wc (workout controls) to individual fields', async () => {
    useStore.getState().update(s => {
      s.wc = { steppers: 0, setShortcuts: 1, pairButtons: 1, exerciseButtons: 0 }
    })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.updateSettings).toHaveBeenCalledWith(expect.objectContaining({
      wcSteppers: 0, wcSetShortcuts: 1, wcPairButtons: 1, wcExerciseButtons: 0
    }))
  })
})

describe('Store Auto-Sync: Routines', () => {
  it('syncs new routine creation', async () => {
    useStore.getState().update(s => {
      s.routines.push({ id: 'r1', name: 'Push', ex: [] })
    })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.createRoutine).toHaveBeenCalled()
  })

  it('syncs routine updates', async () => {
    useStore.getState().update(s => {
      s.routines = [{ id: 'r1', name: 'Push', ex: [] }]
    }, false)

    useStore.getState().update(s => {
      s.routines[0].name = 'Push Day v2'
    })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.updateRoutine).toHaveBeenCalled()
  })

  it('syncs routine deletion', async () => {
    useStore.getState().update(s => {
      s.routines = [{ id: 'r1', name: 'Push', ex: [] }]
    }, false)

    useStore.getState().update(s => {
      s.routines = []
    })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.deleteRoutine).toHaveBeenCalledWith('r1')
  })

  it('does not sync when routines unchanged', () => {
    useStore.getState().update(s => {
      s.theme = 'light' // only settings changed
    })
    expect(mocks.createRoutine).not.toHaveBeenCalled()
    expect(mocks.updateRoutine).not.toHaveBeenCalled()
    expect(mocks.deleteRoutine).not.toHaveBeenCalled()
  })
})

describe('Store Auto-Sync: Week schedule', () => {
  it('syncs week schedule changes', async () => {
    useStore.getState().update(s => { s.week = { 1: ['r1'] } })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.updateWeekSchedule).toHaveBeenCalledWith({ 1: ['r1'] })
  })

  it('does not sync when week unchanged', () => {
    useStore.getState().update(s => { s.theme = 'light' })
    expect(mocks.updateWeekSchedule).not.toHaveBeenCalled()
  })
})

describe('Store Auto-Sync: Bodyweight', () => {
  it('syncs new bodyweight entry', async () => {
    useStore.getState().update(s => {
      s.bodyweight.push({ d: '2026-11-08', w: 73, t: Date.now() })
    })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.addBodyweight).toHaveBeenCalled()
  })

  it('does not sync when bodyweight array shrinks (deletion handled by explicit method)', () => {
    useStore.getState().update(s => {
      s.bodyweight = [{ d: '2026-11-07', w: 73 }, { d: '2026-11-08', w: 74 }]
    }, false)

    useStore.getState().update(s => {
      s.bodyweight = [{ d: '2026-11-08', w: 74 }]
    })
    // Auto-sync only detects additions (length increase), not removals
    expect(mocks.addBodyweight).not.toHaveBeenCalled()
  })
})

describe('Store Auto-Sync: Exercise weights', () => {
  it('syncs exercise weight changes', async () => {
    useStore.getState().update(s => {
      s.exWeights['bench-press'] = { w: 65 }
    })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.setExerciseWeight).toHaveBeenCalledWith('bench-press', 65)
  })

  it('only syncs changed exercise weights', async () => {
    useStore.getState().update(s => {
      s.exWeights = { 'bench-press': { w: 60 }, 'squat': { w: 100 } }
    }, false)

    useStore.getState().update(s => {
      s.exWeights['squat'] = { w: 105 }
    })
    await new Promise(r => setTimeout(r, 10))
    expect(mocks.setExerciseWeight).toHaveBeenCalledTimes(1)
    expect(mocks.setExerciseWeight).toHaveBeenCalledWith('squat', 105)
  })
})

describe('Store Auto-Sync: API failure resilience', () => {
  it('continues working locally when API fails', async () => {
    mocks.updateSettings.mockRejectedValueOnce(new Error('Network error'))

    useStore.getState().update(s => { s.unit = 'lb' })

    // Local state should still be updated
    expect(useStore.getState().S.unit).toBe('lb')
  })

  it('local state is not rolled back on API failure', async () => {
    mocks.createRoutine.mockRejectedValueOnce(new Error('Server error'))

    useStore.getState().update(s => {
      s.routines.push({ id: 'r1', name: 'Push', ex: [] })
    })

    // Routine still exists locally
    expect(useStore.getState().S.routines).toHaveLength(1)
  })
})
