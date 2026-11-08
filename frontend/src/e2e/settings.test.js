// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useStore, DEF } from '../store/useStore.js'

const clone = o => JSON.parse(JSON.stringify(o))

const mockUpdateSettings = vi.fn(() => Promise.resolve({}))
vi.mock('../lib/api.js', () => ({
  api: vi.fn(() => Promise.resolve({})),
  getStoredToken: () => 'test-token',
  setStoredToken: vi.fn(),
  clearToken: vi.fn(),
}))
vi.mock('../lib/api-client.js', () => ({
  updateSettings: (...args) => mockUpdateSettings(...args),
  createRoutine: vi.fn(() => Promise.resolve({})),
  updateRoutine: vi.fn(() => Promise.resolve({})),
  deleteRoutine: vi.fn(() => Promise.resolve({})),
  updateWeekSchedule: vi.fn(() => Promise.resolve({})),
  setExerciseWeight: vi.fn(() => Promise.resolve({})),
  addBodyweight: vi.fn(() => Promise.resolve({})),
}))

beforeEach(() => {
  useStore.setState({ S: clone(DEF), user: { id: 'u1', name: 'Test' }, ready: true })
  mockUpdateSettings.mockClear()
})
afterEach(() => { vi.clearAllMocks() })

describe('Settings: Weight unit', () => {
  it('defaults to kg', () => {
    expect(useStore.getState().S.unit).toBe('kg')
  })

  it('switches to lb', () => {
    useStore.getState().update(s => { s.unit = 'lb' })
    expect(useStore.getState().S.unit).toBe('lb')
  })

  it('syncs unit change to backend', async () => {
    useStore.getState().update(s => { s.unit = 'lb' })
    // Give microtask time
    await new Promise(r => setTimeout(r, 10))
    expect(mockUpdateSettings).toHaveBeenCalledWith(expect.objectContaining({ unit: 'lb' }))
  })
})

describe('Settings: Theme', () => {
  it('defaults to dark', () => {
    expect(useStore.getState().S.theme).toBe('dark')
  })

  it('changes to light', () => {
    useStore.getState().update(s => { s.theme = 'light' })
    expect(useStore.getState().S.theme).toBe('light')
  })

  it('supports system theme', () => {
    useStore.getState().update(s => { s.theme = 'system' })
    expect(useStore.getState().S.theme).toBe('system')
  })
})

describe('Settings: Accent color', () => {
  it('defaults to lime', () => {
    expect(useStore.getState().S.accent).toBe('lime')
  })

  it('changes accent', () => {
    useStore.getState().update(s => { s.accent = 'blue' })
    expect(useStore.getState().S.accent).toBe('blue')
  })
})

describe('Settings: Language', () => {
  it('defaults to en', () => {
    expect(useStore.getState().S.lang).toBe('en')
  })

  it('changes language', () => {
    useStore.getState().update(s => { s.lang = 'es' })
    expect(useStore.getState().S.lang).toBe('es')
  })
})

describe('Settings: Workout view', () => {
  it('defaults to cards', () => {
    expect(useStore.getState().S.workoutView).toBe('cards')
  })

  it('switches between cards/list/compact', () => {
    for (const view of ['list', 'compact', 'cards']) {
      useStore.getState().update(s => { s.workoutView = view })
      expect(useStore.getState().S.workoutView).toBe(view)
    }
  })
})

describe('Settings: Sound', () => {
  it('defaults to true', () => {
    expect(useStore.getState().S.sound).toBe(true)
  })

  it('toggles sound off', () => {
    useStore.getState().update(s => { s.sound = false })
    expect(useStore.getState().S.sound).toBe(false)
  })
})

describe('Settings: Keep screen awake', () => {
  it('defaults to true', () => {
    expect(useStore.getState().S.keepAwake).toBe(true)
  })

  it('toggles off', () => {
    useStore.getState().update(s => { s.keepAwake = false })
    expect(useStore.getState().S.keepAwake).toBe(false)
  })
})

describe('Settings: Weight decimals', () => {
  it('defaults to 1 (0.5 steps)', () => {
    expect(useStore.getState().S.wdec).toBe(1)
  })

  it('changes to 2 (0.25 steps)', () => {
    useStore.getState().update(s => { s.wdec = 2 })
    expect(useStore.getState().S.wdec).toBe(2)
  })
})

describe('Settings: Week start', () => {
  it('defaults to Monday (1)', () => {
    expect(useStore.getState().S.weekStart).toBe(1)
  })

  it('changes to Sunday (0)', () => {
    useStore.getState().update(s => { s.weekStart = 0 })
    expect(useStore.getState().S.weekStart).toBe(0)
  })
})

describe('Settings: Effort tracking', () => {
  it('defaults to null (off)', () => {
    expect(useStore.getState().S.effort).toBeNull()
  })

  it('enables RIR', () => {
    useStore.getState().update(s => { s.effort = 'rir' })
    expect(useStore.getState().S.effort).toBe('rir')
  })

  it('enables RPE', () => {
    useStore.getState().update(s => { s.effort = 'rpe' })
    expect(useStore.getState().S.effort).toBe('rpe')
  })

  it('turns off', () => {
    useStore.getState().update(s => { s.effort = 'rir' })
    useStore.getState().update(s => { s.effort = null })
    expect(useStore.getState().S.effort).toBeNull()
  })
})

describe('Settings: GIF size', () => {
  it('defaults to full', () => {
    expect(useStore.getState().S.gifSize).toBe('full')
  })

  it('cycles through full/small/hidden', () => {
    for (const size of ['small', 'hidden', 'full']) {
      useStore.getState().update(s => { s.gifSize = size })
      expect(useStore.getState().S.gifSize).toBe(size)
    }
  })
})

describe('Settings: Target weight', () => {
  it('defaults to null', () => {
    expect(useStore.getState().S.targetW).toBeNull()
  })

  it('sets a target weight', () => {
    useStore.getState().update(s => { s.targetW = 75 })
    expect(useStore.getState().S.targetW).toBe(75)
  })

  it('clears target weight', () => {
    useStore.getState().update(s => { s.targetW = 75 })
    useStore.getState().update(s => { s.targetW = null })
    expect(useStore.getState().S.targetW).toBeNull()
  })
})

describe('Settings: Multiple settings changed at once', () => {
  it('batches changes in one update', () => {
    useStore.getState().update(s => {
      s.unit = 'lb'
      s.theme = 'light'
      s.accent = 'blue'
      s.lang = 'es'
    })

    const S = useStore.getState().S
    expect(S.unit).toBe('lb')
    expect(S.theme).toBe('light')
    expect(S.accent).toBe('blue')
    expect(S.lang).toBe('es')
  })
})
