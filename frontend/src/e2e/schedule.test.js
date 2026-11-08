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
  updateWeekSchedule: vi.fn(() => Promise.resolve({})),
  updateDayOverride: vi.fn(() => Promise.resolve({})),
  updateSettings: vi.fn(() => Promise.resolve({})),
  createRoutine: vi.fn(() => Promise.resolve({})),
  updateRoutine: vi.fn(() => Promise.resolve({})),
  deleteRoutine: vi.fn(() => Promise.resolve({})),
  setExerciseWeight: vi.fn(() => Promise.resolve({})),
  addBodyweight: vi.fn(() => Promise.resolve({})),
}))

beforeEach(() => {
  useStore.setState({ S: clone(DEF), user: { id: 'u1', name: 'Test' }, ready: true })
})
afterEach(() => { vi.clearAllMocks() })

describe('Schedule: Week schedule', () => {
  it('starts with empty week', () => {
    expect(useStore.getState().S.week).toEqual({})
  })

  it('assigns a routine to a weekday', async () => {
    await useStore.getState().updateWeek({ 1: ['r1'] })
    expect(useStore.getState().S.week).toEqual({ 1: ['r1'] })
  })

  it('assigns multiple routines to a day', async () => {
    await useStore.getState().updateWeek({ 1: ['r1', 'r2'] })
    expect(useStore.getState().S.week[1]).toEqual(['r1', 'r2'])
  })

  it('assigns routines to multiple days', async () => {
    await useStore.getState().updateWeek({ 1: ['r1'], 3: ['r2'], 5: ['r1'] })
    const week = useStore.getState().S.week
    expect(week[1]).toEqual(['r1'])
    expect(week[3]).toEqual(['r2'])
    expect(week[5]).toEqual(['r1'])
  })

  it('clears the entire week schedule', async () => {
    await useStore.getState().updateWeek({ 1: ['r1'], 3: ['r2'] })
    await useStore.getState().updateWeek({})
    expect(useStore.getState().S.week).toEqual({})
  })

  it('replaces existing schedule entirely', async () => {
    await useStore.getState().updateWeek({ 1: ['r1'] })
    await useStore.getState().updateWeek({ 2: ['r2'] })
    const week = useStore.getState().S.week
    expect(week[1]).toBeUndefined()
    expect(week[2]).toEqual(['r2'])
  })
})

describe('Schedule: Day plan overrides', () => {
  it('starts with empty day plan', () => {
    expect(useStore.getState().S.dayPlan).toEqual({})
  })

  it('overrides a specific day', async () => {
    await useStore.getState().updateDayPlan('2026-11-08', 'r2')
    expect(useStore.getState().S.dayPlan['2026-11-08']).toBe('r2')
  })

  it('clears a day override', async () => {
    await useStore.getState().updateDayPlan('2026-11-08', 'r2')
    await useStore.getState().updateDayPlan('2026-11-08', null)
    expect(useStore.getState().S.dayPlan['2026-11-08']).toBeUndefined()
  })

  it('multiple day overrides', async () => {
    await useStore.getState().updateDayPlan('2026-11-08', 'r1')
    await useStore.getState().updateDayPlan('2026-11-09', 'r2')
    expect(useStore.getState().S.dayPlan['2026-11-08']).toBe('r1')
    expect(useStore.getState().S.dayPlan['2026-11-09']).toBe('r2')
  })
})
