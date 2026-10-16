export function fmtDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function lastNDays(n) {
  const to = new Date()
  const from = new Date()
  from.setDate(from.getDate() - (n - 1))
  return [fmtDate(from), fmtDate(to)]
}

export function presetRange(days) {
  const [from, to] = lastNDays(days)
  return { from, to }
}

export function num(v) {
  if (v === null || v === undefined) return 0
  return Number(v)
}

export function compact(v) {
  v = num(v)
  if (v >= 100000000) return (v / 100000000).toFixed(2) + '亿'
  if (v >= 10000) return (v / 10000).toFixed(1) + '万'
  return String(v)
}

export function fmtDuration(seconds) {
  seconds = num(seconds)
  if (seconds < 60) return seconds + '秒'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  if (m < 60) return `${m}分${s}秒`
  const h = Math.floor(m / 60)
  return `${h}时${m % 60}分`
}

export function fmtDateTime(iso) {
  if (!iso) return '-'
  return iso.replace('T', ' ').slice(0, 19)
}
