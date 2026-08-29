// 通用格式化工具

/** 日期格式化为 YYYY-MM-DD HH:mm */
export function fmtDateTime(val) {
  if (!val) return '-'
  const d = new Date(val)
  if (isNaN(d.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 金额格式化为 ¥ 前缀两位小数；后端可能传分或元，约定按元处理 */
export function fmtMoney(val) {
  const n = Number(val)
  if (isNaN(n)) return '¥0.00'
  return `¥${n.toFixed(2)}`
}
