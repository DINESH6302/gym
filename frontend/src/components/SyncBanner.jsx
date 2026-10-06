import { useStore } from '../store/useStore.js'
import { t } from '../lib/i18n.js'
import Icon from './Icon.jsx'

// One line under the header for a signed-in user whose changes are not on the server: offline,
// or a push the server refused. Guests and the standalone phone build have no server to sync
// with, so it never shows for them. Tapping retries at once instead of waiting for the poll.
export default function SyncBanner() {
  // pushState/pullState removed — data persists via REST API now.
  // SyncBanner is a no-op until we add offline detection.
  return null
}
