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
  setExerciseWeight: vi.fn(() => Promise.resolve({})),
  setExerciseNote: vi.fn(() => Promise.resolve({})),
  addFavourite: vi.fn(() => Promise.resolve({})),
  removeFavourite: vi.fn(() => Promise.resolve({})),
  setBarWeight: vi.fn(() => Promise.resolve({})),
  updateSettings: vi.fn(() => Promise.resolve({})),
  createRoutine: vi.fn(() => Promise.resolve({})),
  updateRoutine: vi.fn(() => Promise.resolve({})),
  deleteRoutine: vi.fn(() => Promise.resolve({})),
  updateWeekSchedule: vi.fn(() => Promise.resolve({})),
  addBodyweight: vi.fn(() => Promise.resolve({})),
}))

beforeEach(() => {
  useStore.setState({ S: clone(DEF), user: { id: 'u1', name: 'Test' }, ready: true })
})
afterEach(() => { vi.clearAllMocks() })

describe('Exercise Weights: Tracking', () => {
  it('starts with empty exercise weights', () => {
    expect(useStore.getState().S.exWeights).toEqual({})
  })

  it('sets exercise weight', async () => {
    await useStore.getState().setExWeight('bench-press', 60)
    expect(useStore.getState().S.exWeights['bench-press']).toEqual({ w: 60 })
  })

  it('updates existing exercise weight', async () => {
    await useStore.getState().setExWeight('bench-press', 60)
    await useStore.getState().setExWeight('bench-press', 65)
    expect(useStore.getState().S.exWeights['bench-press']).toEqual({ w: 65 })
  })

  it('tracks weights for multiple exercises independently', async () => {
    await useStore.getState().setExWeight('bench-press', 60)
    await useStore.getState().setExWeight('squat', 100)
    await useStore.getState().setExWeight('deadlift', 120)

    expect(useStore.getState().S.exWeights['bench-press'].w).toBe(60)
    expect(useStore.getState().S.exWeights['squat'].w).toBe(100)
    expect(useStore.getState().S.exWeights['deadlift'].w).toBe(120)
  })

  it('handles zero weight (bodyweight exercises)', async () => {
    await useStore.getState().setExWeight('pull-up', 0)
    expect(useStore.getState().S.exWeights['pull-up']).toEqual({ w: 0 })
  })
})

describe('Exercise Notes', () => {
  it('starts with empty notes', () => {
    expect(useStore.getState().S.exNotes).toEqual({})
  })

  it('adds a note to an exercise', async () => {
    await useStore.getState().setExNote('bench-press', 'Focus on arch')
    expect(useStore.getState().S.exNotes['bench-press']).toBe('Focus on arch')
  })

  it('updates a note', async () => {
    await useStore.getState().setExNote('bench-press', 'Focus on arch')
    await useStore.getState().setExNote('bench-press', 'Wider grip')
    expect(useStore.getState().S.exNotes['bench-press']).toBe('Wider grip')
  })

  it('removes a note by setting empty string', async () => {
    await useStore.getState().setExNote('bench-press', 'Focus on arch')
    await useStore.getState().setExNote('bench-press', '')
    expect(useStore.getState().S.exNotes['bench-press']).toBeUndefined()
  })
})

describe('Favourites', () => {
  it('starts with empty favourites', () => {
    expect(useStore.getState().S.favEx).toEqual([])
  })

  it('adds an exercise to favourites', async () => {
    await useStore.getState().toggleFavourite('bench-press')
    expect(useStore.getState().S.favEx).toContain('bench-press')
  })

  it('removes from favourites on second toggle', async () => {
    await useStore.getState().toggleFavourite('bench-press')
    await useStore.getState().toggleFavourite('bench-press')
    expect(useStore.getState().S.favEx).not.toContain('bench-press')
  })

  it('handles multiple favourites', async () => {
    await useStore.getState().toggleFavourite('bench-press')
    await useStore.getState().toggleFavourite('squat')
    await useStore.getState().toggleFavourite('deadlift')

    expect(useStore.getState().S.favEx).toHaveLength(3)
    expect(useStore.getState().S.favEx).toContain('squat')
  })

  it('toggling one does not affect others', async () => {
    await useStore.getState().toggleFavourite('bench-press')
    await useStore.getState().toggleFavourite('squat')
    await useStore.getState().toggleFavourite('bench-press') // remove

    expect(useStore.getState().S.favEx).toEqual(['squat'])
  })
})

describe('Bar Weights', () => {
  it('starts with empty bar weights', () => {
    expect(useStore.getState().S.barWeights).toEqual({})
  })

  it('sets a custom bar weight', async () => {
    await useStore.getState().setBarWeight('bench-press', 20)
    expect(useStore.getState().S.barWeights['bench-press']).toBe(20)
  })

  it('different exercises can have different bar weights', async () => {
    await useStore.getState().setBarWeight('bench-press', 20)
    await useStore.getState().setBarWeight('squat', 20)
    await useStore.getState().setBarWeight('ohp', 15)

    expect(useStore.getState().S.barWeights['ohp']).toBe(15)
  })
})

describe('Custom Exercises', () => {
  it('starts with empty custom exercises', () => {
    expect(useStore.getState().S.customEx).toEqual([])
  })

  it('adds a custom exercise', async () => {
    await useStore.getState().addCustomExercise({
      id: 'custom-1', name: 'Cable Crunch', tg: 'abs', eq: 'cable'
    })

    expect(useStore.getState().S.customEx).toHaveLength(1)
    expect(useStore.getState().S.customEx[0].name).toBe('Cable Crunch')
  })

  it('removes a custom exercise', async () => {
    await useStore.getState().addCustomExercise({ id: 'c1', name: 'Ex1' })
    await useStore.getState().addCustomExercise({ id: 'c2', name: 'Ex2' })

    await useStore.getState().removeCustomExercise('c1')

    expect(useStore.getState().S.customEx).toHaveLength(1)
    expect(useStore.getState().S.customEx[0].id).toBe('c2')
  })
})

describe('Equipment Profiles', () => {
  it('starts with empty profiles', () => {
    expect(useStore.getState().S.equipProfiles).toEqual([])
  })

  it('adds an equipment profile', async () => {
    await useStore.getState().addEquipProfile({
      id: 'home', name: 'Home Gym', items: ['barbell', 'dumbbells', 'bench']
    })

    expect(useStore.getState().S.equipProfiles).toHaveLength(1)
    expect(useStore.getState().S.equipProfiles[0].name).toBe('Home Gym')
  })

  it('removes a profile and falls back activeEquipId', async () => {
    await useStore.getState().addEquipProfile({ id: 'home', name: 'Home' })
    await useStore.getState().addEquipProfile({ id: 'gym', name: 'Gym' })

    useStore.getState().update(s => { s.activeEquipId = 'home' }, false)
    await useStore.getState().removeEquipProfile('home')

    expect(useStore.getState().S.equipProfiles).toHaveLength(1)
    // activeEquipId falls back to first remaining
    expect(useStore.getState().S.activeEquipId).toBe('gym')
  })
})
