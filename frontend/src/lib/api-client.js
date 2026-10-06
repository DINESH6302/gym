// Typed API client — all CRUD operations call the backend REST API.
// No localStorage for data storage. Only the JWT token is stored locally.
import { api } from './api.js'

// ─── Settings ───────────────────────────────────────────────────────────────
export const getSettings = () => api('/api/settings')
export const updateSettings = (patch) => api('/api/settings', { method: 'PUT', body: JSON.stringify(patch) })

// ─── Routines ───────────────────────────────────────────────────────────────
export const getRoutines = () => api('/api/routines')
export const getRoutine = (id) => api('/api/routines/' + id)
export const createRoutine = (data) => api('/api/routines', { method: 'POST', body: JSON.stringify(data) })
export const updateRoutine = (id, data) => api('/api/routines/' + id, { method: 'PUT', body: JSON.stringify(data) })
export const deleteRoutine = (id) => api('/api/routines/' + id, { method: 'DELETE' })
export const addRoutineExercise = (routineId, data) => api('/api/routines/' + routineId + '/exercises', { method: 'POST', body: JSON.stringify(data) })
export const updateRoutineExercise = (routineId, exId, data) => api('/api/routines/' + routineId + '/exercises/' + exId, { method: 'PUT', body: JSON.stringify(data) })
export const deleteRoutineExercise = (routineId, exId) => api('/api/routines/' + routineId + '/exercises/' + exId, { method: 'DELETE' })

// ─── Workouts ───────────────────────────────────────────────────────────────
export const getWorkouts = (limit = 50, offset = 0) => api('/api/workouts?limit=' + limit + '&offset=' + offset)
export const getWorkout = (id) => api('/api/workouts/' + id)
export const saveWorkout = (data) => api('/api/workouts', { method: 'POST', body: JSON.stringify(data) })
export const deleteWorkout = (id) => api('/api/workouts/' + id, { method: 'DELETE' })

// ─── Body Weight ────────────────────────────────────────────────────────────
export const getBodyweight = () => api('/api/bodyweight')
export const addBodyweight = (data) => api('/api/bodyweight', { method: 'POST', body: JSON.stringify(data) })
export const deleteBodyweight = (id) => api('/api/bodyweight/' + id, { method: 'DELETE' })

// ─── Schedule ───────────────────────────────────────────────────────────────
export const getSchedule = () => api('/api/schedule')
export const updateWeekSchedule = (week) => api('/api/schedule/week', { method: 'PUT', body: JSON.stringify(week) })
export const updateDayOverride = (dateIso, data) => api('/api/schedule/day/' + dateIso, { method: 'PUT', body: JSON.stringify(data) })

// ─── Exercise Data ──────────────────────────────────────────────────────────
export const getExerciseWeights = () => api('/api/exercise-weights')
export const setExerciseWeight = (exerciseId, weight) => api('/api/exercise-weights/' + exerciseId, { method: 'PUT', body: JSON.stringify({ weight }) })
export const getExerciseNotes = () => api('/api/exercise-notes')
export const setExerciseNote = (exerciseId, note) => api('/api/exercise-notes/' + exerciseId, { method: 'PUT', body: JSON.stringify({ note }) })
export const getFavourites = () => api('/api/favourites')
export const addFavourite = (exerciseId) => api('/api/favourites/' + exerciseId, { method: 'POST' })
export const removeFavourite = (exerciseId) => api('/api/favourites/' + exerciseId, { method: 'DELETE' })
export const getBarWeights = () => api('/api/bar-weights')
export const setBarWeight = (exerciseId, weight) => api('/api/bar-weights/' + exerciseId, { method: 'PUT', body: JSON.stringify({ weight }) })

// ─── Equipment Profiles ─────────────────────────────────────────────────────
export const getEquipmentProfiles = () => api('/api/equipment-profiles')
export const createEquipmentProfile = (data) => api('/api/equipment-profiles', { method: 'POST', body: JSON.stringify(data) })
export const updateEquipmentProfile = (id, data) => api('/api/equipment-profiles/' + id, { method: 'PUT', body: JSON.stringify(data) })
export const deleteEquipmentProfile = (id) => api('/api/equipment-profiles/' + id, { method: 'DELETE' })

// ─── Custom Exercises ───────────────────────────────────────────────────────
export const getCustomExercises = () => api('/api/custom-exercises')
export const createCustomExercise = (data) => api('/api/custom-exercises', { method: 'POST', body: JSON.stringify(data) })
export const updateCustomExercise = (id, data) => api('/api/custom-exercises/' + id, { method: 'PUT', body: JSON.stringify(data) })
export const deleteCustomExercise = (id) => api('/api/custom-exercises/' + id, { method: 'DELETE' })
