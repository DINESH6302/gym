// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useStore, DEF } from '../store/useStore.js'
import { buildCompletedWorkout } from '../lib/finish-workout.js'

const clone = o => JSON.parse(JSON.stringify(o))

vi.mock('../lib/api.js', () => ({
  api: vi.fn(() => Promise.resolve({})),
  getStoredToken: () => 'test-token',
  setStoredToken: vi.fn(),
  clearToken: vi.fn(),
}))
vi.mock('../lib/api-client.js', () => ({
  saveWorkout: vi.fn(() => Promise.resolve({})),
  deleteWorkout: vi.fn(() => Promise.resolve({})),
  updateSettings: vi.fn(() => Promise.resolve({})),
  createRoutine: vi.fn(() => Promise.resolve({})),
  updateRoutine: vi.fn(() => Promise.resolve({})),
  deleteRoutine: vi.fn(() => Promise.resolve({})),
  updateWeekSchedule: vi.fn(() => Promise.resolve({})),
  setExerciseWeight: vi.fn(() => Promise.resolve({})),
  addBodyweight: vi.fn(() => Promise.resolve({})),
}))

const makeEntry = (id, sets, opts = {}) => ({
  id, sets, target: { sets: sets.length, reps: 8, mode: 'reps' }, ...opts
})
const makeSet = (w, r, done = true) => ({ w, r, done })

beforeEach(() => {
  useStore.setState({ S: clone(DEF), user: { id: 'u1', name: 'Test' }, ready: true })
})
afterEach(() => { vi.clearAllMocks() })

describe('Workout Session: Start', () => {
  it('starts a workout from a routine', () => {
    useStore.getState().update(s => {
      s.routines = [{
        id: 'r1', name: 'Push', ex: [
          { id: 'bench-press', sets: 3, reps: 10, mode: 'reps' },
          { id: 'ohp', sets: 3, reps: 8, mode: 'reps' },
        ]
      }]
    }, false)

    // Simulate startFlow building active workout
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Push',
        routineIds: ['r1'], cur: 0, bw: null,
        entries: [
          makeEntry('bench-press', [makeSet(0, 10, false), makeSet(0, 10, false), makeSet(0, 10, false)], { rid: 'r1' }),
          makeEntry('ohp', [makeSet(0, 8, false), makeSet(0, 8, false), makeSet(0, 8, false)], { rid: 'r1' }),
        ]
      }
    }, false)

    const A = useStore.getState().S.active
    expect(A).not.toBeNull()
    expect(A.entries).toHaveLength(2)
    expect(A.entries[0].id).toBe('bench-press')
    expect(A.entries[0].sets).toHaveLength(3)
    expect(A.cur).toBe(0)
  })

  it('starts a freestyle workout with no routine', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w2', d: '2026-11-08', start: Date.now(), name: 'Freestyle',
        routineIds: [], cur: 0, bw: null, entries: []
      }
    }, false)

    const A = useStore.getState().S.active
    expect(A.routineIds).toEqual([])
    expect(A.entries).toHaveLength(0)
  })
})

describe('Workout Session: Logging sets', () => {
  it('records weight and reps for a set', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Push',
        routineIds: ['r1'], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(0, 10, false)])]
      }
    }, false)

    useStore.getState().update(s => {
      s.active.entries[0].sets[0].w = 60
      s.active.entries[0].sets[0].r = 10
      s.active.entries[0].sets[0].done = true
    })

    const set = useStore.getState().S.active.entries[0].sets[0]
    expect(set.w).toBe(60)
    expect(set.r).toBe(10)
    expect(set.done).toBe(true)
  })

  it('toggles a set done/undone', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(60, 10, false)])]
      }
    }, false)

    // Toggle on
    useStore.getState().update(s => { s.active.entries[0].sets[0].done = true })
    expect(useStore.getState().S.active.entries[0].sets[0].done).toBe(true)

    // Toggle off
    useStore.getState().update(s => { s.active.entries[0].sets[0].done = false })
    expect(useStore.getState().S.active.entries[0].sets[0].done).toBe(false)
  })

  it('adds a set to an exercise mid-session', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(60, 10, true)])]
      }
    }, false)

    useStore.getState().update(s => {
      s.active.entries[0].sets.push(makeSet(60, 10, false))
    })

    expect(useStore.getState().S.active.entries[0].sets).toHaveLength(2)
  })

  it('removes a set (cannot go below 1)', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(60, 10, true), makeSet(60, 8, true)])]
      }
    }, false)

    useStore.getState().update(s => {
      if (s.active.entries[0].sets.length > 1) s.active.entries[0].sets.pop()
    })

    expect(useStore.getState().S.active.entries[0].sets).toHaveLength(1)
  })

  it('zero weight is allowed (bodyweight exercises)', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [makeEntry('pull-up', [makeSet(0, 12, true)])]
      }
    }, false)

    expect(useStore.getState().S.active.entries[0].sets[0].w).toBe(0)
    expect(useStore.getState().S.active.entries[0].sets[0].done).toBe(true)
  })
})

describe('Workout Session: Add/Remove exercises mid-session', () => {
  it('adds an exercise mid-session', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(60, 10, false)])]
      }
    }, false)

    useStore.getState().update(s => {
      s.active.entries.push(makeEntry('lateral-raise', [makeSet(10, 15, false)]))
    })

    expect(useStore.getState().S.active.entries).toHaveLength(2)
    expect(useStore.getState().S.active.entries[1].id).toBe('lateral-raise')
  })

  it('added exercise has no rid (not part of routine progression)', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Push',
        routineIds: ['r1'], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(60, 10, false)], { rid: 'r1' })]
      }
    }, false)

    useStore.getState().update(s => {
      s.active.entries.push(makeEntry('lateral-raise', [makeSet(10, 15, false)]))
      // No rid — freestyle addition
    })

    expect(useStore.getState().S.active.entries[1].rid).toBeUndefined()
  })

  it('removes an exercise mid-session', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [
          makeEntry('bench-press', [makeSet(60, 10, true)]),
          makeEntry('ohp', [makeSet(40, 8, false)]),
        ]
      }
    }, false)

    useStore.getState().update(s => {
      s.active.entries.splice(1, 1)
      if (s.active.cur >= s.active.entries.length) s.active.cur = Math.max(0, s.active.entries.length - 1)
    })

    expect(useStore.getState().S.active.entries).toHaveLength(1)
    expect(useStore.getState().S.active.entries[0].id).toBe('bench-press')
  })

  it('removing current exercise adjusts cursor', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 1, bw: null,
        entries: [
          makeEntry('bench-press', [makeSet(60, 10, true)]),
          makeEntry('ohp', [makeSet(40, 8, false)]),
        ]
      }
    }, false)

    useStore.getState().update(s => {
      s.active.entries.splice(1, 1)
      if (s.active.cur >= s.active.entries.length) s.active.cur = Math.max(0, s.active.entries.length - 1)
    })

    expect(useStore.getState().S.active.cur).toBe(0)
  })

  it('does not modify routine template when adding exercise to session', () => {
    useStore.getState().update(s => {
      s.routines = [{ id: 'r1', name: 'Push', ex: [{ id: 'bench-press', sets: 3, reps: 10 }] }]
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Push',
        routineIds: ['r1'], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(60, 10, false)], { rid: 'r1' })]
      }
    }, false)

    useStore.getState().update(s => {
      s.active.entries.push(makeEntry('flyes', [makeSet(15, 12, false)]))
    })

    // Routine template unchanged
    expect(useStore.getState().S.routines[0].ex).toHaveLength(1)
    expect(useStore.getState().S.routines[0].ex[0].id).toBe('bench-press')
    // Session has 2
    expect(useStore.getState().S.active.entries).toHaveLength(2)
  })
})

describe('Workout Session: Skip exercise', () => {
  it('skips an exercise and unchecks all sets', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(60, 10, true), makeSet(60, 8, true)])]
      }
    }, false)

    useStore.getState().update(s => {
      const e = s.active.entries[0]
      e.skipped = true
      e.sets.forEach(set => { set.done = false })
    })

    const entry = useStore.getState().S.active.entries[0]
    expect(entry.skipped).toBe(true)
    expect(entry.sets.every(s => !s.done)).toBe(true)
  })

  it('unskips an exercise', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(60, 10, false)])]
      }
      s.active.entries[0].skipped = true
    }, false)

    useStore.getState().update(s => {
      s.active.entries[0].skipped = false
    })

    expect(useStore.getState().S.active.entries[0].skipped).toBe(false)
  })

  it('skipped exercises are excluded from completed workout', () => {
    const active = {
      id: 'w1', d: '2026-11-08', start: 1000, routineIds: ['r1'], name: 'Push', bw: null,
      entries: [
        { id: 'bench-press', sets: [makeSet(60, 10, true)], target: { sets: 1, reps: 10 } },
        { id: 'ohp', sets: [makeSet(40, 8, true)], target: { sets: 1, reps: 8 }, skipped: true },
        { id: 'flyes', sets: [makeSet(15, 12, true)], target: { sets: 1, reps: 12 } },
      ]
    }

    const completed = buildCompletedWorkout(active, { end: 2000, prs: [] })
    expect(completed.entries).toHaveLength(2)
    expect(completed.entries.map(e => e.id)).toEqual(['bench-press', 'flyes'])
  })
})

describe('Workout Session: Finish', () => {
  it('builds a completed workout with correct shape', () => {
    const active = {
      id: 'w1', d: '2026-11-08', start: 1000, routineIds: ['r1'], name: 'Push', bw: 73,
      entries: [
        { id: 'bench-press', sets: [makeSet(60, 10, true), makeSet(60, 8, true)], target: { sets: 2, reps: 10 }, rid: 'r1' },
      ]
    }

    const completed = buildCompletedWorkout(active, { end: 2000, prs: [] })

    expect(completed.id).toBe('w1')
    expect(completed.d).toBe('2026-11-08')
    expect(completed.start).toBe(1000)
    expect(completed.end).toBe(2000)
    expect(completed.routineIds).toEqual(['r1'])
    expect(completed.routineId).toBe('r1')
    expect(completed.name).toBe('Push')
    expect(completed.bw).toBe(73)
    expect(completed.entries).toHaveLength(1)
    expect(completed.entries[0].rid).toBe('r1')
  })

  it('freestyle workout has null routineId', () => {
    const active = {
      id: 'w2', d: '2026-11-08', start: 1000,
      entries: [{ id: 'curl', sets: [makeSet(15, 12, true)], target: { sets: 1, reps: 12 } }]
    }

    const completed = buildCompletedWorkout(active, { end: 2000 })
    expect(completed.routineIds).toEqual([])
    expect(completed.routineId).toBeNull()
  })

  it('excludes entries with no completed work', () => {
    const active = {
      id: 'w3', d: '2026-11-08', start: 1000, routineIds: [],
      entries: [
        { id: 'bench-press', sets: [makeSet(60, 10, true)], target: { sets: 1, reps: 10 } },
        { id: 'ohp', sets: [makeSet(0, 0, false)], target: { sets: 1, reps: 8 } }, // nothing done
      ]
    }

    const completed = buildCompletedWorkout(active, { end: 2000 })
    expect(completed.entries).toHaveLength(1)
    expect(completed.entries[0].id).toBe('bench-press')
  })

  it('saves workout to store and clears active', async () => {
    const workout = {
      id: 'w1', d: '2026-11-08', start: 1000, end: 2000,
      routineIds: ['r1'], name: 'Push', bw: 73,
      entries: [{ id: 'bench-press', sets: [makeSet(60, 10, true)] }]
    }

    await useStore.getState().saveWorkout(workout)

    expect(useStore.getState().S.workouts).toHaveLength(1)
    expect(useStore.getState().S.workouts[0].id).toBe('w1')
    expect(useStore.getState().S.active).toBeNull()
  })

  it('removes a workout from history', async () => {
    useStore.getState().update(s => {
      s.workouts = [
        { id: 'w1', d: '2026-11-07', entries: [] },
        { id: 'w2', d: '2026-11-08', entries: [] },
      ]
    }, false)

    await useStore.getState().removeWorkout('w1')

    expect(useStore.getState().S.workouts).toHaveLength(1)
    expect(useStore.getState().S.workouts[0].id).toBe('w2')
  })
})

describe('Workout Session: Navigation', () => {
  it('navigates between exercises with cursor', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [
          makeEntry('bench-press', [makeSet(60, 10, false)]),
          makeEntry('ohp', [makeSet(40, 8, false)]),
          makeEntry('flyes', [makeSet(15, 12, false)]),
        ]
      }
    }, false)

    // Move to next
    useStore.getState().update(s => { s.active.cur = 1 })
    expect(useStore.getState().S.active.cur).toBe(1)

    // Move to last
    useStore.getState().update(s => { s.active.cur = 2 })
    expect(useStore.getState().S.active.cur).toBe(2)

    // Move back
    useStore.getState().update(s => { s.active.cur = 0 })
    expect(useStore.getState().S.active.cur).toBe(0)
  })

  it('cursor cannot exceed entry count', () => {
    useStore.getState().update(s => {
      s.active = {
        id: 'w1', d: '2026-11-08', start: Date.now(), name: 'Test',
        routineIds: [], cur: 0, bw: null,
        entries: [makeEntry('bench-press', [makeSet(60, 10, false)])]
      }
    }, false)

    useStore.getState().update(s => {
      s.active.cur = Math.min(5, s.active.entries.length - 1)
    })

    expect(useStore.getState().S.active.cur).toBe(0)
  })
})
