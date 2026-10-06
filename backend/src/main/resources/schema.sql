-- =============================================================================
-- openGym Normalized Schema v2
-- SQLite — TEXT for dates/timestamps, INTEGER for booleans
-- =============================================================================

-- ─── USERS ──────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id              TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    email           TEXT NOT NULL UNIQUE,
    password_hash   TEXT NOT NULL,
    admin           INTEGER NOT NULL DEFAULT 0,
    disabled        INTEGER NOT NULL DEFAULT 0,
    created_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

-- ─── USER SETTINGS (1:1 with users) ────────────────────────────────────────

CREATE TABLE IF NOT EXISTS user_settings (
    user_id             TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    unit                TEXT NOT NULL DEFAULT 'kg',
    sound               INTEGER NOT NULL DEFAULT 1,
    sound_on_silent     INTEGER NOT NULL DEFAULT 0,
    keep_awake          INTEGER NOT NULL DEFAULT 1,
    lang                TEXT NOT NULL DEFAULT 'en',
    theme               TEXT NOT NULL DEFAULT 'dark',
    accent              TEXT NOT NULL DEFAULT 'lime',
    body                TEXT NOT NULL DEFAULT 'male',
    target_weight       REAL,
    gif_size            TEXT NOT NULL DEFAULT 'full',
    workout_view        TEXT NOT NULL DEFAULT 'cards',
    effort              TEXT,
    reminder_on         INTEGER NOT NULL DEFAULT 0,
    reminder_time       TEXT DEFAULT '08:00',
    reminder_tz         TEXT,
    auto_backup         INTEGER NOT NULL DEFAULT 0,
    equip_filter_on     INTEGER NOT NULL DEFAULT 0,
    active_equip_id     TEXT,
    weigh_in            INTEGER NOT NULL DEFAULT 1,
    week_start          INTEGER NOT NULL DEFAULT 1,
    weight_decimals     INTEGER NOT NULL DEFAULT 1,
    wc_steppers         INTEGER NOT NULL DEFAULT 1,
    wc_set_shortcuts    INTEGER NOT NULL DEFAULT 0,
    wc_pair_buttons     INTEGER NOT NULL DEFAULT 0,
    wc_exercise_buttons INTEGER NOT NULL DEFAULT 0,
    updated_at          TEXT NOT NULL DEFAULT (datetime('now')),
    rev                 INTEGER NOT NULL DEFAULT 0
);

-- ─── BODYWEIGHT ENTRIES ────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS bodyweight_entries (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date_iso    TEXT NOT NULL,
    weight      REAL NOT NULL,
    timestamp   INTEGER,
    created_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_bw_user_date ON bodyweight_entries(user_id, date_iso);
CREATE INDEX IF NOT EXISTS idx_bw_user_ts   ON bodyweight_entries(user_id, timestamp);

-- ─── EXERCISE LIBRARY (static, shared across all users) ───────────────────

CREATE TABLE IF NOT EXISTS exercise_library (
    id              TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    body_part       TEXT,
    equipment       TEXT,
    target          TEXT,
    muscle_group    TEXT,
    description     TEXT,
    image_file      TEXT,
    gif_file        TEXT
);

CREATE TABLE IF NOT EXISTS exercise_library_muscles (
    exercise_id     TEXT NOT NULL REFERENCES exercise_library(id) ON DELETE CASCADE,
    muscle_slug     TEXT NOT NULL,
    role            TEXT NOT NULL DEFAULT 'secondary',
    PRIMARY KEY (exercise_id, muscle_slug, role)
);

CREATE TABLE IF NOT EXISTS exercise_library_steps (
    exercise_id     TEXT NOT NULL REFERENCES exercise_library(id) ON DELETE CASCADE,
    step_order      INTEGER NOT NULL,
    instruction     TEXT NOT NULL,
    PRIMARY KEY (exercise_id, step_order)
);

-- ─── CUSTOM EXERCISES (per user) ──────────────────────────────────────────

CREATE TABLE IF NOT EXISTS custom_exercises (
    id              TEXT NOT NULL,
    user_id         TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            TEXT NOT NULL,
    body_part       TEXT,
    equipment       TEXT,
    target          TEXT,
    description     TEXT,
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (user_id, id)
);

CREATE TABLE IF NOT EXISTS custom_exercise_muscles (
    user_id         TEXT NOT NULL,
    exercise_id     TEXT NOT NULL,
    muscle_slug     TEXT NOT NULL,
    role            TEXT NOT NULL DEFAULT 'secondary',
    PRIMARY KEY (user_id, exercise_id, muscle_slug, role),
    FOREIGN KEY (user_id, exercise_id) REFERENCES custom_exercises(user_id, id) ON DELETE CASCADE
);

-- ─── ROUTINES ──────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS routines (
    id                          TEXT NOT NULL,
    user_id                     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name                        TEXT NOT NULL,
    emoji                       TEXT,
    progression_policy          TEXT DEFAULT 'linear',
    exclude_from_progression    INTEGER NOT NULL DEFAULT 0,
    sort_order                  INTEGER NOT NULL DEFAULT 0,
    created_at                  TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (user_id, id)
);

CREATE INDEX IF NOT EXISTS idx_routines_user ON routines(user_id, sort_order);

-- ─── ROUTINE EXERCISES ─────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS routine_exercises (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         TEXT NOT NULL,
    routine_id      TEXT NOT NULL,
    exercise_id     TEXT NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    superset_group  TEXT,
    sets            INTEGER DEFAULT 3,
    reps            INTEGER DEFAULT 10,
    reps_min        INTEGER,
    reps_max        INTEGER,
    weight          REAL DEFAULT 0,
    mode            TEXT DEFAULT 'reps',
    sec             INTEGER,
    min_duration    INTEGER,
    speed           REAL,
    bodyweight      INTEGER NOT NULL DEFAULT 0,
    per_side        INTEGER NOT NULL DEFAULT 0,
    assisted        INTEGER,
    rest_sec        INTEGER,
    warmup_rest_sec INTEGER,
    warmup_sets     INTEGER DEFAULT 0,
    progression     TEXT,
    increment       REAL,
    deload_factor   REAL,
    note            TEXT,
    FOREIGN KEY (user_id, routine_id) REFERENCES routines(user_id, id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_routine_ex ON routine_exercises(user_id, routine_id, sort_order);

-- ─── ROUTINE EXERCISE INTENSIFIERS ────────────────────────────────────────

CREATE TABLE IF NOT EXISTS routine_exercise_intensifiers (
    routine_exercise_id INTEGER PRIMARY KEY REFERENCES routine_exercises(id) ON DELETE CASCADE,
    type                TEXT NOT NULL,
    drop_count          INTEGER,
    drop_pct            INTEGER,
    total_reps          INTEGER,
    rest_sec            INTEGER
);

-- ─── WEEKLY SCHEDULE ───────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS week_schedule (
    user_id     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    weekday     INTEGER NOT NULL,
    routine_id  TEXT NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, weekday, routine_id)
);

-- ─── DAY PLAN OVERRIDES ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS day_plan_overrides (
    user_id     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date_iso    TEXT NOT NULL,
    routine_id  TEXT,
    PRIMARY KEY (user_id, date_iso)
);

-- ─── WORKOUTS (completed sessions) ────────────────────────────────────────

CREATE TABLE IF NOT EXISTS workouts (
    id                          TEXT NOT NULL,
    user_id                     TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date_iso                    TEXT NOT NULL,
    start_time                  INTEGER,
    end_time                    INTEGER,
    name                        TEXT,
    bodyweight                  REAL,
    note                        TEXT,
    exclude_from_progression    INTEGER NOT NULL DEFAULT 0,
    created_at                  TEXT NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (user_id, id)
);

CREATE INDEX IF NOT EXISTS idx_workouts_user_date  ON workouts(user_id, date_iso);
CREATE INDEX IF NOT EXISTS idx_workouts_user_start ON workouts(user_id, start_time);

-- ─── WORKOUT <-> ROUTINE link ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS workout_routines (
    user_id     TEXT NOT NULL,
    workout_id  TEXT NOT NULL,
    routine_id  TEXT NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, workout_id, routine_id),
    FOREIGN KEY (user_id, workout_id) REFERENCES workouts(user_id, id) ON DELETE CASCADE
);

-- ─── WORKOUT ENTRIES (one per exercise in a workout) ──────────────────────

CREATE TABLE IF NOT EXISTS workout_entries (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         TEXT NOT NULL,
    workout_id      TEXT NOT NULL,
    exercise_id     TEXT NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    top_weight      REAL,
    routine_id      TEXT,
    no_progression  INTEGER NOT NULL DEFAULT 0,
    note            TEXT,
    note_pin        INTEGER NOT NULL DEFAULT 0,
    superset_group  TEXT,
    target_json     TEXT,
    muscle_snapshot_json TEXT,
    FOREIGN KEY (user_id, workout_id) REFERENCES workouts(user_id, id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_wentries_user_exercise ON workout_entries(user_id, exercise_id);
CREATE INDEX IF NOT EXISTS idx_wentries_workout ON workout_entries(user_id, workout_id, sort_order);

-- ─── WORKOUT SETS ─────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS workout_sets (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    entry_id        INTEGER NOT NULL REFERENCES workout_entries(id) ON DELETE CASCADE,
    set_order       INTEGER NOT NULL,
    weight          REAL,
    reps            INTEGER,
    done            INTEGER NOT NULL DEFAULT 0,
    phase           TEXT NOT NULL DEFAULT 'work',
    mode            TEXT DEFAULT 'reps',
    seconds         INTEGER,
    minutes         REAL,
    speed           REAL,
    rir             REAL,
    rpe             REAL,
    set_type        TEXT NOT NULL DEFAULT 'straight',
    is_per_side     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_sets_entry ON workout_sets(entry_id, set_order);

-- ─── SET SIDES (unilateral L/R) ───────────────────────────────────────────

CREATE TABLE IF NOT EXISTS set_sides (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    set_id          INTEGER NOT NULL REFERENCES workout_sets(id) ON DELETE CASCADE,
    side            TEXT NOT NULL,
    weight          REAL,
    reps            INTEGER,
    done            INTEGER NOT NULL DEFAULT 0,
    rir             REAL,
    rpe             REAL,
    set_type        TEXT NOT NULL DEFAULT 'straight',
    UNIQUE (set_id, side)
);

-- ─── SET DROPS (drop-set weight drops) ────────────────────────────────────

CREATE TABLE IF NOT EXISTS set_drops (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    set_id          INTEGER REFERENCES workout_sets(id) ON DELETE CASCADE,
    side_id         INTEGER REFERENCES set_sides(id) ON DELETE CASCADE,
    drop_order      INTEGER NOT NULL,
    weight          REAL NOT NULL DEFAULT 0,
    reps            INTEGER NOT NULL DEFAULT 0,
    CHECK ((set_id IS NOT NULL AND side_id IS NULL) OR (set_id IS NULL AND side_id IS NOT NULL))
);

CREATE INDEX IF NOT EXISTS idx_drops_set  ON set_drops(set_id);
CREATE INDEX IF NOT EXISTS idx_drops_side ON set_drops(side_id);

-- ─── SET CLUSTERS (rest-pause bursts) ─────────────────────────────────────

CREATE TABLE IF NOT EXISTS set_clusters (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    set_id          INTEGER REFERENCES workout_sets(id) ON DELETE CASCADE,
    side_id         INTEGER REFERENCES set_sides(id) ON DELETE CASCADE,
    cluster_order   INTEGER NOT NULL,
    reps            INTEGER NOT NULL DEFAULT 0,
    rest_sec        INTEGER NOT NULL DEFAULT 0,
    CHECK ((set_id IS NOT NULL AND side_id IS NULL) OR (set_id IS NULL AND side_id IS NOT NULL))
);

CREATE INDEX IF NOT EXISTS idx_clusters_set  ON set_clusters(set_id);
CREATE INDEX IF NOT EXISTS idx_clusters_side ON set_clusters(side_id);

-- ─── WORKOUT PERSONAL RECORDS ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS workout_prs (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         TEXT NOT NULL,
    workout_id      TEXT NOT NULL,
    exercise_id     TEXT NOT NULL,
    pr_type         TEXT,
    value           REAL,
    FOREIGN KEY (user_id, workout_id) REFERENCES workouts(user_id, id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_prs_user_exercise ON workout_prs(user_id, exercise_id);

-- ─── ACTIVE WORKOUT (transient, device-local, JSON blob) ──────────────────

CREATE TABLE IF NOT EXISTS active_workout (
    user_id         TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    session_json    TEXT NOT NULL DEFAULT '{}',
    updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

-- ─── EXERCISE WEIGHTS (confirmed working weight) ──────────────────────────

CREATE TABLE IF NOT EXISTS exercise_weights (
    user_id         TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id     TEXT NOT NULL,
    weight          REAL NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, exercise_id)
);

-- ─── EXERCISE NOTES ───────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS exercise_notes (
    user_id         TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id     TEXT NOT NULL,
    note            TEXT NOT NULL,
    PRIMARY KEY (user_id, exercise_id)
);

-- ─── FAVOURITE EXERCISES ──────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS favourite_exercises (
    user_id         TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id     TEXT NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, exercise_id)
);

-- ─── BAR WEIGHTS ──────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS bar_weights (
    user_id         TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id     TEXT NOT NULL,
    weight          REAL NOT NULL,
    PRIMARY KEY (user_id, exercise_id)
);

-- ─── EQUIPMENT PROFILES ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS equipment_profiles (
    id              TEXT NOT NULL,
    user_id         TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            TEXT NOT NULL,
    PRIMARY KEY (user_id, id)
);

CREATE TABLE IF NOT EXISTS equipment_profile_items (
    user_id         TEXT NOT NULL,
    profile_id      TEXT NOT NULL,
    equipment       TEXT NOT NULL,
    PRIMARY KEY (user_id, profile_id, equipment),
    FOREIGN KEY (user_id, profile_id) REFERENCES equipment_profiles(user_id, id) ON DELETE CASCADE
);


-- ─── LEGACY STATE (kept for backward-compatible sync during migration) ────

CREATE TABLE IF NOT EXISTS user_state (
    user_id    TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    state_json TEXT NOT NULL DEFAULT '{}',
    rev        INTEGER NOT NULL DEFAULT 0,
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);
