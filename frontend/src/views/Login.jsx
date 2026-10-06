import { useStore } from '../store/useStore.js'
import { useUI } from '../store/useUI.js'
import { registerUser, loginUser } from '../lib/api.js'
import { t } from '../lib/i18n.js'
import { useState, useRef, useEffect } from 'react'
import Icon from '../components/Icon.jsx'
import { Button } from '../components/ui.jsx'

function RegisterSheet({ close }) {
  const { setUser, boot } = useStore()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const ref = useRef(null)
  useEffect(() => { setTimeout(() => ref.current?.focus(), 250) }, [])
  const go = async () => {
    if (!name.trim()) { useUI.getState().toast(t('Enter your name')); return }
    if (!email.trim() || !email.includes('@')) { useUI.getState().toast(t('Enter a valid email')); return }
    if (password.length < 6) { useUI.getState().toast(t('Password must be at least 6 characters')); return }
    try {
      await registerUser(name.trim(), email.trim(), password)
      close()
      await boot()
      useUI.getState().toast(t('Welcome, {0}!', useStore.getState().user?.name))
    } catch (e) { useUI.getState().toast(e.message || t('Registration failed')) }
  }
  return <>
    <h3>{t('Create Account')}</h3>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 12 }}>
      <input ref={ref} className="input" placeholder={t('Name')} maxLength={40} value={name}
        onChange={e => setName(e.target.value)} />
      <input className="input" type="email" placeholder={t('Email')} value={email}
        onChange={e => setEmail(e.target.value)} />
      <input className="input" type="password" placeholder={t('Password (min 6 chars)')} value={password}
        onChange={e => setPassword(e.target.value)} onKeyDown={e => e.key === 'Enter' && go()} />
    </div>
    <Button variant="primary" onClick={go}>{t('Create Account')}</Button>
  </>
}

function LoginSheet({ close }) {
  const { boot } = useStore()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const ref = useRef(null)
  useEffect(() => { setTimeout(() => ref.current?.focus(), 250) }, [])
  const go = async () => {
    if (!email.trim()) { useUI.getState().toast(t('Enter your email')); return }
    if (!password) { useUI.getState().toast(t('Enter your password')); return }
    try {
      await loginUser(email.trim(), password)
      close()
      await boot()
      useUI.getState().toast(t('Welcome back, {0}!', useStore.getState().user?.name))
    } catch (e) { useUI.getState().toast(e.message || t('Login failed')) }
  }
  return <>
    <h3>{t('Login')}</h3>
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 12 }}>
      <input ref={ref} className="input" type="email" placeholder={t('Email')} value={email}
        onChange={e => setEmail(e.target.value)} />
      <input className="input" type="password" placeholder={t('Password')} value={password}
        onChange={e => setPassword(e.target.value)} onKeyDown={e => e.key === 'Enter' && go()} />
    </div>
    <Button variant="primary" onClick={go}>{t('Login')}</Button>
  </>
}

export default function Login() {
  const wrap = { display: 'flex', flexDirection: 'column', justifyContent: 'center', minHeight: '78vh', textAlign: 'center' }

  return (
    <div className="narrow" style={wrap}>
      <div style={{ fontSize: 54, display: 'flex', justifyContent: 'center', color: 'var(--acc)' }}><Icon name="dumbbell" /></div>
      <h1 style={{ fontSize: 34, fontWeight: 700, letterSpacing: '-.028em', margin: '10px 0 4px' }}>openGym</h1>
      <div className="muted" style={{ marginBottom: 34 }}>{t('Your workouts. Your weights. Your profile.')}</div>

      <Button variant="primary" icon="sparkles" onClick={() => useUI.getState().openSheet(close => <RegisterSheet close={close} />)}>
        {t('Create Account')}
      </Button>
      <div style={{ height: 10 }} />
      <Button icon="person" onClick={() => useUI.getState().openSheet(close => <LoginSheet close={close} />)}>
        {t('Login')}
      </Button>

      <div className="dim small" style={{ marginTop: 26, lineHeight: 1.5 }}>
        {t('Create an account to sync your data across devices.')}
      </div>
    </div>
  )
}
