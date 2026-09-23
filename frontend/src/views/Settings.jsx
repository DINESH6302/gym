import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useStore, DEF } from '../store/useStore.js'
import { workoutControls } from '../lib/workout-controls.js'
import { convertStateUnit } from '../lib/units.js'
import { useUI } from '../store/useUI.js'
import { ACCENTS, localTZ, weekStartOf, MONDAY, SUNDAY } from '../lib/format.js'
import { effortOf } from '../lib/history.js'
import { unlock, playOnSilentSupported } from '../lib/sound.js'
import { IS_ANDROID } from '../lib/api.js'
import { pushSupported, enablePush, disablePush, sendTestPush, syncPushSubscription } from '../lib/push.js'
import { wakeLockSupported } from '../lib/wakelock.js'
import { t, LANGS, INSTR_LANGS } from '../lib/i18n.js'
import { MOBILE, syncReminder } from '../lib/mobile.js'
import { confirmSheet, equipmentProfileSheet, menuSheet } from '../sheets.jsx'
import Icon from '../components/Icon.jsx'
import { Section, Row, SelectRow, Switch, Segmented, Button } from '../components/ui.jsx'

export default function Settings() {
  const nav = useNavigate()
  const S = useStore(s => s.S)
  const user = useStore(s => s.user)
  const { update, replaceState } = useStore()
  const toast = useUI(s => s.toast)
  const wakeOK = wakeLockSupported()

  // Two honest choices on a unit switch (issue #22): convert the numbers, or keep them and only
  // change the label — the old behaviour, still right for someone who logged in lb all along
  // under a kg label. Closing the sheet leaves the unit as it was.
  const switchUnit = v => {
    if (v === S.unit) return
    menuSheet({
      title: t('Convert to {0}?', v),
      subtitle: t('Every stored weight — logged sets, working weights, routine targets, body weight, bar weights — is in {0}. Convert the numbers, or keep them and only change the label?', S.unit),
      items: [
        { icon: 'shuffle', label: t('Convert the numbers'), onClick: () => replaceState(convertStateUnit(useStore.getState().S, v)) },
        { icon: 'pencil', label: t('Keep the numbers, change the label'), onClick: () => update(s => { s.unit = v }) },
      ],
    })
  }

  return <div className="narrow">
    <div className="hdr">
      <button className="iconbtn" onClick={() => nav('/home')} aria-label={t('Home')}><Icon name="chevronLeft" /></button>
      <div style={{ flex: 1, marginLeft: 10 }}><h1>{t('Settings')}</h1></div>
    </div>

    {/* ---------- general ---------- */}
    <Section title={t('General')} footer={t('Switching the unit offers to convert every stored weight.')}>
      <SelectRow
        icon="globe" iconTint="var(--blue)" title={t('Language')}
        value={S.lang || 'en'} onChange={v => update(s => { s.lang = v })}
        options={Object.entries(LANGS).map(([k, name]) => ({
          value: k, label: name,
          subtitle: INSTR_LANGS.includes(k) ? null : t("Exercise instructions aren't available in this language yet — they stay in English."),
        }))}
      />
      <Row icon="scale" iconTint="var(--teal)" title={t('Weight unit')}>
        <Segmented className="seg-inline"
          options={[{ value: 'kg', label: 'kg' }, { value: 'lb', label: 'lb' }]}
          value={S.unit} onChange={v => switchUnit(v)} />
      </Row>
      {/* Display only: one decimal reads fine for plate-loadable numbers, two for anyone whose
          per-side figure lands on .25 or .75, or who loads microplates (issue #139). Nothing is
          stored or rounded differently — lib/format.js fmtNum just prints what is already there. */}
      <Row icon="plate" iconTint="var(--teal)" title={t('Weight decimals')} subtitle={t('How precisely weights are shown.')}>
        <Segmented className="seg-inline"
          options={[{ value: 1, label: t('0.5') }, { value: 2, label: t('0.25') }]}
          value={S.wdec === 2 ? 2 : 1} onChange={v => update(s => { s.wdec = v })} />
      </Row>
      {/* Monday or Sunday — the Plan list, the Home strip, the calendar grid and every
          "this week" total follow it. Stored as a getDay() index (see lib/format.js). */}
      <Row icon="calendar" iconTint="var(--orange)" title={t('Week starts on')}>
        <Segmented className="seg-inline"
          options={[{ value: MONDAY, label: t('Monday') }, { value: SUNDAY, label: t('Sunday') }]}
          value={weekStartOf(S)} onChange={v => update(s => { s.weekStart = v })} />
      </Row>
      {/* Membership QR codes on Home (views/CheckIn.jsx); off = no Home card, no route. */}
      <Row icon="qr" iconTint="var(--blue)" title={t('Gym check-in')}
        subtitle={t('Show a card on Home with your membership QR codes.')}>
        <Switch checked={S.checkIn !== false} onChange={v => update(s => { s.checkIn = v })} />
      </Row>
    </Section>

    {/* ---------- during a workout ---------- */}
    <Section title={t('During a workout')} footer={wakeOK ? t('The screen stays on while a workout is running, so you don’t have to unlock your phone between sets.') : null}>
      {/* The quick weigh-in that opens on Start (sheets.jsx startFlow, issue #137); off skips straight
          to the session. Home and Stats still log weight by hand. */}
      <Row icon="scale" iconTint="var(--green)" title={t('Weigh in before workouts')}
        subtitle={t('Asks for your body weight when a workout starts. Off starts the session straight away.')}>
        <Switch checked={S.weighIn !== false} onChange={v => update(s => { s.weighIn = v })} />
      </Row>
      {/* One exercise at a time (cards with Prev/Next), the whole session stacked as a
          scrollable list, or that list stripped to just names and set rows (compact).
          Legacy/unknown values read as cards. The running session can override this from
          the workout header's ⋮ menu without changing this default. */}
      <Row icon="list" iconTint="var(--blue)" title={t('Workout view')}>
        <Segmented className="seg-inline"
          options={[{ value: 'cards', label: t('Cards') }, { value: 'list', label: t('List') }, { value: 'compact', label: t('Compact') }]}
          value={['list', 'compact'].includes(S.workoutView) ? S.workoutView : 'cards'}
          onChange={v => update(s => { s.workoutView = v })} />
      </Row>
      {/* The lean workout screen keeps the sets and one "more" button per exercise; each switch
          brings one of the old always-visible button groups back for people who liked them. */}
      <Row icon="wrench" iconTint="var(--purple)" title={t('Workout controls')} accessory="chevron"
        subtitle={t('Everything hidden here stays one tap away: the ⋯ button of an exercise and the number of a set.')}
        onClick={() => workoutControlsSheet()} />
      <SelectRow icon="timer" iconTint="var(--orange)" title={t('Rest timer')}
        value={S.restSec} onChange={v => update(s => { s.restSec = v })}
        options={[{ value: 0, label: t('Off') }, ...[60, 90, 120, 150, 180].map(v => ({ value: v, label: v + 's' }))]} />
      {/* Default for a rest-pause burst added live on a plain set — a planned exercise's own
          "Rest (s)" (in its Intensifier config) overrides this, same as the main rest timer
          is the fallback whenever an exercise has no progression rule of its own. */}
      <SelectRow icon="bolt" iconTint="var(--acc)" title={t('Rest-pause rest')}
        value={S.restPauseSec} onChange={v => update(s => { s.restPauseSec = v })}
        options={[10, 15, 20, 30].map(v => ({ value: v, label: v + 's' }))} />
      {(wakeOK || !MOBILE) && (
        <Row icon="sun" iconTint="var(--yellow)" title={t('Keep screen awake')}
          subtitle={wakeOK ? null : t('Not supported in this browser.')}>
          <Switch checked={wakeOK && S.keepAwake !== false} disabled={!wakeOK}
            onChange={v => update(s => { s.keepAwake = v })} />
        </Row>
      )}
      {/* 'full'/'mini' is also what the tap-toggle on the workout animation writes; 'off' hides
          workout media entirely (library, detail sheet and picker thumbs are unaffected).
          Legacy/unknown values read as 'full'. */}
      <Row icon="figureRun" iconTint="var(--green)" title={t('Exercise animations')}>
        <Segmented className="seg-inline"
          options={[{ value: 'full', label: t('Full') }, { value: 'mini', label: t('Small') }, { value: 'off', label: t('Hidden') }]}
          value={S.gifSize === 'mini' || S.gifSize === 'off' ? S.gifSize : 'full'}
          onChange={v => update(s => { s.gifSize = v })} />
      </Row>
      <Row icon="bell" iconTint="var(--pink)" title={t('Sounds')}>
        {/* Turning Sounds on is a tap: unlock the audio context now so a timer that ends before
            the next set check can already sound (iOS, #152). */}
        <Switch checked={!!S.sound} onChange={v => { if (v) unlock(true); update(s => { s.sound = v }) }} />
      </Row>
      {/* iOS only (WebKit's audio-session API, iOS 17+): with it off the ring/silent switch mutes
          the timer. On, the phone treats the timer like a music player — exclusive, and the
          music app is not told it may resume — so it is a choice, off by default (lib/sound.js). */}
      {S.sound && playOnSilentSupported() && (
        <Row icon="bell" iconTint="var(--orange)" title={t('Play sounds when the phone is on silent')}
          subtitle={t('Music playing on this phone stops during a workout and does not resume by itself.')}>
          <Switch checked={!!S.soundOnSilent} onChange={v => update(s => { s.soundOnSilent = v })} />
        </Row>
      )}
      <Row icon="sun" iconTint="var(--yellow)" title={t('Flash screen when timer ends')}>
        <Switch checked={!!S.timerFlash} onChange={v => update(s => { s.timerFlash = v })} />
      </Row>
      {/* Two names for the same judgement, so the column asks in the scale you already think in.
          The (i) sits before the control — you read it on the way to the choice, not after it. */}
      <Row icon="target" iconTint="var(--purple)" title={t('Effort per set')}>
        <button className="helpbtn" aria-label={t('What are RIR and RPE?')} onClick={effortHelpSheet}><Icon name="info" /></button>
        <Segmented className="seg-inline"
          options={[{ value: 'none', label: t('Off') }, { value: 'rir', label: t('RIR') }, { value: 'rpe', label: t('RPE') }]}
          value={effortOf(S)} onChange={v => update(s => { s.effort = v; delete s.showRir })} />
      </Row>
    </Section>

    {(user || MOBILE) && <NotificationsCard S={S} update={update} toast={toast} />}

    {/* ---------- equipment ---------- */}
    <EquipmentCard S={S} update={update} />

    {/* ---------- appearance ---------- */}
    <Section title={t('Appearance')} footer={MOBILE ? undefined : t('synced with your profile')}>
      <Row icon="moon" iconTint="var(--indigo)" title={t('Theme')}>
        <Segmented
          className="seg-inline"
          options={[
            { value: 'dark', icon: 'moon', label: t('Dark') },
            { value: 'light', icon: 'sun', label: t('Light') },
            { value: 'system', icon: 'gear', label: t('System') },
          ]}
          value={S.theme || 'dark'}
          onChange={v => update(s => { s.theme = v })}
        />
      </Row>
      {/* Purely how the muscle map is drawn — nothing else in the app reads this. */}
      <Row icon="figureStrength" iconTint="var(--teal)" title={t('Body diagram')}>
        <Segmented
          className="seg-inline"
          options={[{ value: 'male', label: t('Male') }, { value: 'female', label: t('Female') }]}
          value={S.body === 'female' ? 'female' : 'male'}
          onChange={v => update(s => { s.body = v })}
        />
      </Row>
      <div className="lrow" style={{ flexDirection: 'column', alignItems: 'stretch', gap: 12, paddingTop: 13, paddingBottom: 14 }}>
        <span className="lrow-t">{t('Accent color')}</span>
        <div className="swatches">
          {Object.entries(ACCENTS).map(([k, c]) => (
            <button key={k} className={'swatch' + ((S.accent || 'lime') === k ? ' on' : '')}
              style={{ background: c }} onClick={() => update(s => { s.accent = k })} aria-label={k} />
          ))}
        </div>
      </div>
    </Section>


    {/* The version, at the bottom of Settings — which is where the support template has been
        telling people to look for it, and where it was not. On the phone build there is no
        address bar and no about box, so without this there is no way to tell which build you
        are running, or whether an update actually installed. */}
    <div className="dim small" style={{ textAlign: 'center', marginTop: 4, lineHeight: 1.6 }}>
      openGym v{__APP_VERSION__} · {t('free & open source (AGPL v3)')}<br />
      <a href="https://gitlab.com/DuarteSantos8/opengym" target="_blank" rel="noopener">source code</a> · exercise data: hasaneyldrm/exercises-dataset (MIT)<br />
      exercise images and animations © <a href="https://gymvisual.com/" target="_blank" rel="noopener">Gym visual</a>
    </div>
  </div>
}

// The whole point is that the two scales are one judgement counted from opposite ends, and a
// paragraph is a bad way to say that — the conversion table shows it in one look. Reading down
// a column is the answer to "what do I put here", so the numbers get their own aligned columns.
const EFFORT_ROWS = [
  ['0', '10', 'Nothing left — went to failure'],
  ['1', '9', 'One more rep in the tank'],
  ['2', '8', 'Two more reps'],
  ['3', '7', 'Three more reps'],
  ['4+', '≤6', 'Easy — warm-up territory'],
]
// RIR 2 / RPE 8: the row a working set usually lands on — the anchor the others are read
// against. Not where the stepper starts; + walks up from the bottom of the scale.
const EFFORT_TYPICAL = 2

// Settings → During a workout → Workout controls. S.wc overlays DEF.wc, so a profile from
// before this setting existed reads as the lean default.
function WorkoutControlsSheet() {
  const S = useStore(s => s.S)
  const update = useStore(s => s.update)
  const wc = workoutControls(S)
  const set = (k, v) => update(s => { s.wc = { ...workoutControls(s), [k]: v } })
  return <>
    <h3>{t('Workout controls')}</h3>
    <div className="muted small" style={{ marginBottom: 12 }}>{t('Everything hidden here stays one tap away: the ⋯ button of an exercise and the number of a set.')}</div>
    <Section>
      <Row icon="plus" iconTint="var(--acc)" title={t('Weight and reps buttons')} subtitle={t('Off: tap the number and type it')}>
        <Switch checked={wc.steppers} onChange={v => set('steppers', v)} />
      </Row>
      <Row icon="bolt" iconTint="var(--orange)" title={t('Drop and burst shortcuts on every set')}>
        <Switch checked={wc.setShortcuts} onChange={v => set('setShortcuts', v)} />
      </Row>
      <Row icon="link" iconTint="var(--blue)" title={t('Superset buttons in the exercise header')}>
        <Switch checked={wc.pairButtons} onChange={v => set('pairButtons', v)} />
      </Row>
      <Row icon="shuffle" iconTint="var(--teal)" title={t('Move, swap and remove buttons below the exercise')}>
        <Switch checked={wc.exerciseButtons} onChange={v => set('exerciseButtons', v)} />
      </Row>
    </Section>
  </>
}
function workoutControlsSheet() {
  useUI.getState().openSheet(() => <WorkoutControlsSheet />)
}


function effortHelpSheet() {
  useUI.getState().openSheet(close => <>
    <h3>{t('Effort per set')}</h3>
    <div className="muted small" style={{ lineHeight: 1.5 }}>
      {t('How hard a set was, logged next to weight and reps. Two scales for the same judgement, counted from opposite ends.')}
    </div>
    <div className="efftbl">
      <div className="r hd"><span className="n">{t('RIR')}</span><span className="n">{t('RPE')}</span><span className="f">{t('How it felt')}</span></div>
      {EFFORT_ROWS.map(([rir, rpe, feel], i) => (
        <div key={rir} className={'r' + (i === EFFORT_TYPICAL ? ' on' : '')}>
          <span className="n">{rir}</span><span className="n">{rpe}</span><span className="f">{t(feel)}</span>
        </div>
      ))}
    </div>
    <div className="dim small" style={{ lineHeight: 1.5, display: 'grid', gap: 8 }}>
      <div>{t('RIR counts the reps you left; RPE reads the same effort off a 10-point scale — so RPE ≈ 10 − RIR. Pick the one you already think in.')}</div>
      <div>{t('The highlighted row is where most working sets land. Sets you have already logged keep their own scale, and nothing else reads the value — progression and estimated 1RM are unaffected.')}</div>
    </div>
    <div style={{ height: 8 }} />
  </>)
}

function NotificationsCard({ S, update, toast }) {
  if (MOBILE) return <MobileReminderCard S={S} update={update} toast={toast} />
  return <PushCard S={S} update={update} toast={toast} />
}

// Mobile build: the reminder is a native local notification scheduled on planned weekdays —
// no push server involved. The schedule itself is (re)synced by the store on every persist;
// this card only owns the OS permission prompt when the switch turns on.
function MobileReminderCard({ S, update, toast }) {
  const setReminder = patch => update(s => { s.reminder = { ...(s.reminder || DEF.reminder), ...patch, tz: localTZ() } })
  const toggle = async () => {
    const on = !S.reminder?.on
    if (on) {
      const ok = await syncReminder({ ...S, reminder: { ...(S.reminder || DEF.reminder), on: true } }, true)
      if (!ok) { toast(t('Could not change notification settings')); return }
    }
    setReminder({ on })
  }
  return (
    <Section title={t('Notifications')}
      footer={S.reminder?.on ? t('Reminds you at this time on days that have a routine planned.') : null}>
      <Row icon="calendar" iconTint="var(--orange)" title={t('Workout day reminder')}>
        <Switch checked={!!S.reminder?.on} onChange={toggle} />
      </Row>
      {S.reminder?.on && (
        <Row icon="clock" iconTint="var(--purple)" title={t('Reminder time')}>
          <input type="time" className="timef" value={S.reminder?.time || DEF.reminder.time}
            onChange={e => setReminder({ time: e.target.value })} />
        </Row>
      )}
    </Section>
  )
}

function PushCard({ S, update, toast }) {
  const [on, setOn] = useState(false)
  const [busy, setBusy] = useState(false)
  const supported = pushSupported()

  // "On" means the server holds this browser's subscription, not merely that the browser has
  // one: a row the instance dropped (dead send, rebuilt db.json) left the switch on with nothing
  // ever arriving. syncPushSubscription re-registers on the way; if the server cannot be asked
  // (offline), the browser's side is the best answer available.
  useEffect(() => {
    if (!supported) return
    let gone = false
    syncPushSubscription()
      .then(ok => { if (!gone) setOn(ok) })
      .catch(() => navigator.serviceWorker.ready.then(reg => reg.pushManager.getSubscription()).then(sub => { if (!gone) setOn(!!sub) }).catch(() => {}))
    return () => { gone = true }
  }, [supported])

  const toggle = async v => {
    setBusy(true)
    try {
      if (!v) { await disablePush(); setOn(false); toast(t('Notifications off')) }
      else { await enablePush(); setOn(true); toast(t('Notifications on')) }
    } catch (e) { toast(e.message || t('Could not change notification settings')) }
    setBusy(false)
  }
  const test = async () => {
    try { await sendTestPush(); toast(t('Test sent — should arrive any second')) }
    catch (e) { toast(e.message || t('Test failed')) }
  }

  if (!supported) return (
    <Section title={t('Notifications')}>
      <Row icon="bellSlash" iconTint="var(--grey)" title={t('Not supported in this browser.')} />
    </Section>
  )

  return <>
    <Section
      title={t('Notifications')}
      footer={on && S.reminder?.on
        ? t("Only sent on days you have a routine planned and haven't logged a workout yet.") +
          (S.reminder?.tz ? ' ' + t('Timezone: {0} (auto-detected, updates if you travel).', S.reminder.tz) : '')
        : null}
    >
      <Row icon="bell" iconTint="var(--red)" title={t('Push notifications')} subtitle={t('Rest-timer alerts, even if openGym is closed.')}>
        <Switch checked={on} disabled={busy} onChange={toggle} />
      </Row>
      {on && (
        <Row icon="calendar" iconTint="var(--orange)" title={t('Workout day reminder')}>
          <Switch checked={!!S.reminder?.on} onChange={() => update(s => { s.reminder = { ...(s.reminder || DEF.reminder), on: !s.reminder?.on, tz: localTZ() } })} />
        </Row>
      )}
      {on && S.reminder?.on && (
        <Row icon="clock" iconTint="var(--purple)" title={t('Reminder time')}>
          <input type="time" className="timef" value={S.reminder?.time || DEF.reminder.time}
            onChange={e => update(s => { s.reminder = { ...(s.reminder || DEF.reminder), time: e.target.value, tz: localTZ() } })} />
        </Row>
      )}
    </Section>
    {on && <div style={{ marginTop: -12, marginBottom: 22 }}><Button size="sm" icon="bell" onClick={test}>{t('Send test notification')}</Button></div>}
  </>
}

// Equipment profiles ("Home", "Gym", ...) — each an id/name/eq-list; the active one filters
// the Library, exercise picker, and flags routine entries that need something outside it
// (see lib/equipment.js). Purely local/synced state — no server changes needed.
function EquipmentCard({ S, update }) {
  const profiles = S.equipProfiles || []
  const remove = p => confirmSheet({
    title: t('Delete profile?'), message: t('"{0}" and its equipment list will be removed.', p.name),
    confirmText: t('Delete'), danger: true,
    onConfirm: () => update(s => {
      s.equipProfiles = (s.equipProfiles || []).filter(x => x.id !== p.id)
      if (s.activeEquipId === p.id) s.activeEquipId = (s.equipProfiles[0] && s.equipProfiles[0].id) || null
    }),
  })
  return <Section title={t('Equipment')} footer={t('Filters the exercise library and picker, and flags routine exercises that need something you don’t have in the active profile.')}>
    {profiles.length > 0 && <Row icon="dumbbell" iconTint="var(--acc)" title={t('Filter by equipment')}>
      <Switch checked={!!S.equipFilterOn} onChange={v => update(s => { s.equipFilterOn = v })} />
    </Row>}
    {profiles.length > 0 && <SelectRow icon="list" iconTint="var(--blue)" title={t('Active profile')}
      value={S.activeEquipId || ''} onChange={v => update(s => { s.activeEquipId = v })}
      options={profiles.map(p => ({ value: p.id, label: p.name }))} />}
    {profiles.map(p => (
      <Row key={p.id} icon="dumbbell" iconTint="var(--teal)" title={p.name}
        subtitle={t('{0} equipment types', p.equipment.length)} accessory="chevron"
        onClick={() => equipmentProfileSheet(p)}>
        <button className="iconbtn" aria-label={t('Delete')} onClick={ev => { ev.stopPropagation(); remove(p) }}><Icon name="trash" /></button>
      </Row>
    ))}
    <Row icon="plus" iconTint="var(--acc)" title={t('Add equipment profile')} accessory="chevron" onClick={() => equipmentProfileSheet(null)} />
  </Section>
}

