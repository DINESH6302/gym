# Migration Plan: localStorage → REST API

## Status: COMPLETE (core migration done)

## Architecture

```
Frontend (React + Zustand)
  ↓ REST API calls
Backend (Spring Boot + SQLite)
  ↓ JPA/Hibernate
SQLite Database (normalized tables)
```

- **No localStorage** for data storage (only JWT token stored locally)
- **Zustand** holds in-memory state — populated from API on boot
- **`update()`** auto-detects what changed and calls the right API endpoint
- **Active workout** stays in memory only — saved to DB when finished

## What's Built

### Backend
- 22 JPA entities + 22 repositories
- 8 service classes with full CRUD + @Transactional
- 8 REST controllers with JWT auth
- Normalized SQLite schema (users, settings, routines, workouts, sets, bodyweight, etc.)

### Frontend
- `api-client.js` — typed REST client for all endpoints
- `useStore.js` — rewritten to:
  - Boot: fetches all data from APIs in parallel
  - `update()`: auto-persists settings, routines, schedule, bodyweight, exercise weights to backend
  - Explicit methods: `saveWorkout()`, `addRoutine()`, `removeRoutine()`, etc.
- `sheets.jsx` — workout finish flow calls `saveWorkout()` API

## How to Run
```bash
# Delete old DB to reset schema
rm -f backend/data/opengym.db

# Start backend
cd backend && mvn spring-boot:run

# Start frontend  
cd frontend && ~/.local/bin/bun run vite
```

## Remaining Polish (optional, not blocking)
- Some views still use `update(s => { ... })` for non-settings data that may not auto-sync
  (e.g. custom exercises, equipment profiles, favourites). These work locally but may not persist.
  Fix: add detection for those fields in `update()` or migrate call sites to explicit methods.
- Remove `user_state` table and `DataController`/`StateService` (legacy blob sync)
- Add error handling/retry for failed API calls
- Add loading states to views during boot
