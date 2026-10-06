import { create } from 'zustand'
import { api } from '../lib/api.js'
import { localTZ } from '../lib/format.js'
import { registerCustom } from '../lib/exercises.js'
import * as AC from '../lib/api-client.js'
import { WC_DEFAULT } from '../lib/workout-controls.js'

// Default state shape — used as fallback for missing fields.
export const DEF = {
  unit: 'kg', sound: true, soundOnSilent: false, keepAwake: true, lang: 'en',
  theme: 'dark', accent: 'lime', body: 'male', targetW: null,
  bodyweight: [], routines: [], week: {}, dayPlan: {},
  exWeights: {}, workouts: [], active: null, customEx: [], gifSize: 'full',
  workoutView: 'cards',
  wc: { ...WC_DEFAULT },
  reminder: { on: false, time: '08:00', tz: null }, effort: null,
  equipProfiles: [], activeEquipId: null, equipFilterOn: false,
  exNotes: {}, favEx: [], weekStart: 1, wdec: 1, barWeights: {},
  weighIn: true,
}
const clone = o => JSON.parse(JSON.stringify(o))
const hasData = st => !!((st.workouts || []).length || (st.routines || []).length || (st.bodyweight || []).length)

export const useStore = create((set, get) => {
  return {
    S: clone(DEF),
    user: null,
    ready: false,
    sync: { offline: false, pending: false, lastSynced: 0 },
    config: null,

    // ── Core state mutation ───────────────────────────────────────────────
    // Update S in memory via a producer function. Detects what changed and
    // persists to the backend API automatically.
    update(mut, push = true) {
      const before = get().S
      const S = clone(before)
      mut(S)
      registerCustom(S.customEx)
      set({ S })

      // Auto-persist changes to backend
      if (push && get().user) {
        // Settings
        const settingsFields = ['unit', 'sound', 'soundOnSilent', 'keepAwake', 'lang', 'theme', 'accent', 'body', 'targetW', 'gifSize', 'workoutView', 'effort', 'weighIn', 'weekStart', 'wdec', 'equipFilterOn', 'activeEquipId', 'wc', 'reminder']
        const patch = {}
        for (const f of settingsFields) {
          if (JSON.stringify(S[f]) !== JSON.stringify(before[f])) patch[f] = S[f]
        }
        if (Object.keys(patch).length) {
          const mapped = {}
          for (const [k, v] of Object.entries(patch)) {
            if (k === 'targetW') mapped.targetWeight = v
            else if (k === 'wdec') mapped.weightDecimals = v
            else if (k === 'wc') {
              mapped.wcSteppers = v.steppers; mapped.wcSetShortcuts = v.setShortcuts
              mapped.wcPairButtons = v.pairButtons; mapped.wcExerciseButtons = v.exerciseButtons
            } else if (k === 'reminder') {
              mapped.reminderOn = v.on; mapped.reminderTime = v.time; mapped.reminderTz = v.tz
            } else mapped[k] = v
          }
          AC.updateSettings(mapped).catch(e => console.warn('settings sync failed:', e))
        }

        // Routines — detect added/updated/removed
        if (JSON.stringify(S.routines) !== JSON.stringify(before.routines)) {
          // New routines
          for (const r of S.routines) {
            if (!before.routines.find(br => br.id === r.id)) {
              AC.createRoutine(r).catch(e => console.warn('routine create failed:', e))
            } else if (JSON.stringify(r) !== JSON.stringify(before.routines.find(br => br.id === r.id))) {
              AC.updateRoutine(r.id, r).catch(e => console.warn('routine update failed:', e))
            }
          }
          // Deleted routines
          for (const r of before.routines) {
            if (!S.routines.find(sr => sr.id === r.id)) {
              AC.deleteRoutine(r.id).catch(e => console.warn('routine delete failed:', e))
            }
          }
        }

        // Week schedule
        if (JSON.stringify(S.week) !== JSON.stringify(before.week)) {
          AC.updateWeekSchedule(S.week).catch(e => console.warn('week sync failed:', e))
        }

        // Bodyweight
        if (S.bodyweight.length > before.bodyweight.length) {
          const newEntry = S.bodyweight[S.bodyweight.length - 1]
          AC.addBodyweight(newEntry).catch(e => console.warn('bodyweight sync failed:', e))
        }

        // Exercise weights
        if (JSON.stringify(S.exWeights) !== JSON.stringify(before.exWeights)) {
          for (const [id, v] of Object.entries(S.exWeights)) {
            if (JSON.stringify(v) !== JSON.stringify(before.exWeights[id])) {
              AC.setExerciseWeight(id, v.w || v).catch(() => {})
            }
          }
        }
      }
    },

    replaceState(S) {
      registerCustom(S.customEx)
      set({ S: clone(S) })
    },

    // ── Auth ──────────────────────────────────────────────────────────────
    setUser(u) { set({ user: u }) },

    async signOut() {
      try { await api('/api/logout', { method: 'POST', body: '{}' }) } catch {}
      const { clearToken } = await import('../lib/api.js')
      clearToken()
      set({ user: null, S: clone(DEF), ready: false })
    },

    // ── Settings (persisted to DB) ────────────────────────────────────────
    async updateSetting(patch) {
      const S = clone(get().S)
      Object.assign(S, patch)
      set({ S })
      try { await AC.updateSettings(patch) } catch (e) { console.warn('settings save failed:', e) }
    },

    // ── Routines (persisted to DB) ────────────────────────────────────────
    async addRoutine(routine) {
      const S = clone(get().S)
      S.routines.push(routine)
      set({ S })
      try { await AC.createRoutine(routine) } catch (e) { console.warn('routine create failed:', e) }
    },
    async updateRoutine(id, data) {
      const S = clone(get().S)
      const idx = S.routines.findIndex(r => r.id === id)
      if (idx >= 0) { Object.assign(S.routines[idx], data); set({ S }) }
      try { await AC.updateRoutine(id, data) } catch (e) { console.warn('routine update failed:', e) }
    },
    async removeRoutine(id) {
      const S = clone(get().S)
      S.routines = S.routines.filter(r => r.id !== id)
      // Also remove from week schedule
      for (const day of Object.keys(S.week)) {
        S.week[day] = (S.week[day] || []).filter(rid => rid !== id)
        if (!S.week[day].length) delete S.week[day]
      }
      set({ S })
      try { await AC.deleteRoutine(id) } catch (e) { console.warn('routine delete failed:', e) }
    },

    // ── Workouts (persisted to DB) ────────────────────────────────────────
    async saveWorkout(workout) {
      const S = clone(get().S)
      S.workouts.push(workout)
      S.active = null
      set({ S })
      try { await AC.saveWorkout(workout) } catch (e) { console.warn('workout save failed:', e) }
    },
    async removeWorkout(id) {
      const S = clone(get().S)
      S.workouts = S.workouts.filter(w => w.id !== id)
      set({ S })
      try { await AC.deleteWorkout(id) } catch (e) { console.warn('workout delete failed:', e) }
    },

    // ── Body Weight (persisted to DB) ─────────────────────────────────────
    async addBodyweight(entry) {
      const S = clone(get().S)
      S.bodyweight.push(entry)
      set({ S })
      try { await AC.addBodyweight(entry) } catch (e) { console.warn('bodyweight save failed:', e) }
    },
    async removeBodyweight(idx) {
      const S = clone(get().S)
      const entry = S.bodyweight[idx]
      S.bodyweight.splice(idx, 1)
      set({ S })
      if (entry?.id) {
        try { await AC.deleteBodyweight(entry.id) } catch (e) { console.warn('bodyweight delete failed:', e) }
      }
    },

    // ── Schedule (persisted to DB) ────────────────────────────────────────
    async updateWeek(week) {
      const S = clone(get().S)
      S.week = week
      set({ S })
      try { await AC.updateWeekSchedule(week) } catch (e) { console.warn('schedule save failed:', e) }
    },
    async updateDayPlan(dateIso, routineId) {
      const S = clone(get().S)
      if (routineId) S.dayPlan[dateIso] = routineId
      else delete S.dayPlan[dateIso]
      set({ S })
      try { await AC.updateDayOverride(dateIso, { routineId }) } catch (e) { console.warn('day plan save failed:', e) }
    },

    // ── Exercise Data (persisted to DB) ───────────────────────────────────
    async setExWeight(exerciseId, weight) {
      const S = clone(get().S)
      S.exWeights[exerciseId] = { w: weight }
      set({ S })
      try { await AC.setExerciseWeight(exerciseId, weight) } catch (e) { console.warn('ex weight save failed:', e) }
    },
    async setExNote(exerciseId, note) {
      const S = clone(get().S)
      if (note) S.exNotes[exerciseId] = note
      else delete S.exNotes[exerciseId]
      set({ S })
      try { await AC.setExerciseNote(exerciseId, note || '') } catch (e) { console.warn('ex note save failed:', e) }
    },
    async toggleFavourite(exerciseId) {
      const S = clone(get().S)
      const idx = S.favEx.indexOf(exerciseId)
      if (idx >= 0) {
        S.favEx.splice(idx, 1)
        set({ S })
        try { await AC.removeFavourite(exerciseId) } catch {}
      } else {
        S.favEx.push(exerciseId)
        set({ S })
        try { await AC.addFavourite(exerciseId) } catch {}
      }
    },
    async setBarWeight(exerciseId, weight) {
      const S = clone(get().S)
      S.barWeights[exerciseId] = weight
      set({ S })
      try { await AC.setBarWeight(exerciseId, weight) } catch (e) { console.warn('bar weight save failed:', e) }
    },

    // ── Equipment Profiles (persisted to DB) ──────────────────────────────
    async addEquipProfile(profile) {
      const S = clone(get().S)
      S.equipProfiles.push(profile)
      set({ S })
      try { await AC.createEquipmentProfile(profile) } catch (e) { console.warn('equip create failed:', e) }
    },
    async updateEquipProfile(id, data) {
      const S = clone(get().S)
      const idx = S.equipProfiles.findIndex(p => p.id === id)
      if (idx >= 0) { Object.assign(S.equipProfiles[idx], data); set({ S }) }
      try { await AC.updateEquipmentProfile(id, data) } catch (e) { console.warn('equip update failed:', e) }
    },
    async removeEquipProfile(id) {
      const S = clone(get().S)
      S.equipProfiles = S.equipProfiles.filter(p => p.id !== id)
      if (S.activeEquipId === id) S.activeEquipId = S.equipProfiles[0]?.id || null
      set({ S })
      try { await AC.deleteEquipmentProfile(id) } catch (e) { console.warn('equip delete failed:', e) }
    },

    // ── Custom Exercises (persisted to DB) ────────────────────────────────
    async addCustomExercise(ex) {
      const S = clone(get().S)
      S.customEx.push(ex)
      registerCustom(S.customEx)
      set({ S })
      try { await AC.createCustomExercise(ex) } catch (e) { console.warn('custom ex create failed:', e) }
    },
    async updateCustomExercise(id, data) {
      const S = clone(get().S)
      const idx = S.customEx.findIndex(e => e.id === id)
      if (idx >= 0) { Object.assign(S.customEx[idx], data); registerCustom(S.customEx); set({ S }) }
      try { await AC.updateCustomExercise(id, data) } catch (e) { console.warn('custom ex update failed:', e) }
    },
    async removeCustomExercise(id) {
      const S = clone(get().S)
      S.customEx = S.customEx.filter(e => e.id !== id)
      registerCustom(S.customEx)
      set({ S })
      try { await AC.deleteCustomExercise(id) } catch (e) { console.warn('custom ex delete failed:', e) }
    },

    // ── Config ────────────────────────────────────────────────────────────
    async loadConfig() {
      if (get().config) return get().config
      try { const c = await api('/api/config'); set({ config: c }); return c }
      catch { return null }
    },

    // ── Boot: load everything from the API ────────────────────────────────
    async boot() {
      const { getStoredToken, clearToken } = await import('../lib/api.js')
      if (!getStoredToken()) {
        // No token — show Login screen
        set({ ready: true })
        return
      }

      try {
        const me = await api('/api/me')
        set({ user: me.user })

        // Fetch all data from the API in parallel
        const [settings, routines, workouts, bodyweight, schedule, exWeights, exNotes, favs, barWeights, equipProfiles, customEx] = await Promise.all([
          AC.getSettings().catch(() => ({})),
          AC.getRoutines().catch(() => []),
          AC.getWorkouts(9999, 0).catch(() => []),
          AC.getBodyweight().catch(() => []),
          AC.getSchedule().catch(() => ({ week: {}, dayOverrides: {} })),
          AC.getExerciseWeights().catch(() => ({})),
          AC.getExerciseNotes().catch(() => ({})),
          AC.getFavourites().catch(() => []),
          AC.getBarWeights().catch(() => ({})),
          AC.getEquipmentProfiles().catch(() => []),
          AC.getCustomExercises().catch(() => []),
        ])

        // Assemble into the S shape the frontend expects
        const S = {
          ...clone(DEF),
          // Settings
          unit: settings.unit || DEF.unit,
          sound: settings.sound ?? DEF.sound,
          soundOnSilent: settings.soundOnSilent ?? DEF.soundOnSilent,
          keepAwake: settings.keepAwake ?? DEF.keepAwake,
          lang: settings.lang || DEF.lang,
          theme: settings.theme || DEF.theme,
          accent: settings.accent || DEF.accent,
          body: settings.body || DEF.body,
          targetW: settings.targetWeight ?? DEF.targetW,
          gifSize: settings.gifSize || DEF.gifSize,
          workoutView: settings.workoutView || DEF.workoutView,
          effort: settings.effort ?? DEF.effort,
          weighIn: settings.weighIn ?? DEF.weighIn,
          weekStart: settings.weekStart ?? DEF.weekStart,
          wdec: settings.weightDecimals ?? DEF.wdec,
          equipFilterOn: !!settings.equipFilterOn,
          activeEquipId: settings.activeEquipId || null,
          wc: {
            steppers: settings.wcSteppers ?? WC_DEFAULT.steppers,
            setShortcuts: settings.wcSetShortcuts ?? WC_DEFAULT.setShortcuts,
            pairButtons: settings.wcPairButtons ?? WC_DEFAULT.pairButtons,
            exerciseButtons: settings.wcExerciseButtons ?? WC_DEFAULT.exerciseButtons,
          },
          reminder: {
            on: !!settings.reminderOn,
            time: settings.reminderTime || DEF.reminder.time,
            tz: settings.reminderTz || null,
          },
          // Data
          routines: Array.isArray(routines) ? routines : [],
          workouts: Array.isArray(workouts) ? workouts : [],
          bodyweight: Array.isArray(bodyweight) ? bodyweight : [],
          week: schedule.week || {},
          dayPlan: schedule.dayOverrides || {},
          exWeights: exWeights || {},
          exNotes: exNotes || {},
          favEx: Array.isArray(favs) ? favs : [],
          barWeights: barWeights || {},
          equipProfiles: Array.isArray(equipProfiles) ? equipProfiles : [],
          customEx: Array.isArray(customEx) ? customEx : [],
          active: null,
        }

        registerCustom(S.customEx)
        set({ S, ready: true })
      } catch (e) {
        if (e.status === 401) { clearToken(); set({ user: null }) }
        set({ ready: true })
      }
    }
  }
})

export { hasData }
