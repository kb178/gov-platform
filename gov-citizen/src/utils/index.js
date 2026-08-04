/**
 * 日期格式化
 * @param {Date|string|number} date - 日期
 * @param {string} fmt - 格式，默认 YYYY-MM-DD HH:mm:ss
 * @returns {string}
 */
export function formatDate(date, fmt = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  const d = new Date(date)
  const opt = {
    'YYYY': d.getFullYear(),
    'MM': String(d.getMonth() + 1).padStart(2, '0'),
    'DD': String(d.getDate()).padStart(2, '0'),
    'HH': String(d.getHours()).padStart(2, '0'),
    'mm': String(d.getMinutes()).padStart(2, '0'),
    'ss': String(d.getSeconds()).padStart(2, '0')
  }
  let result = fmt
  for (const [key, value] of Object.entries(opt)) {
    result = result.replace(key, value)
  }
  return result
}

/**
 * 手机号脱敏
 * @param {string} phone - 手机号
 * @returns {string}
 */
export function maskPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 身份证号脱敏
 * @param {string} idCard - 身份证号
 * @returns {string}
 */
export function maskIdCard(idCard) {
  if (!idCard) return ''
  return idCard.replace(/(\d{4})\d{10}(\w{4})/, '$1**********$2')
}

/**
 * 防抖函数
 * @param {Function} fn - 目标函数
 * @param {number} delay - 延迟时间(ms)
 * @returns {Function}
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} fn - 目标函数
 * @param {number} delay - 延迟时间(ms)
 * @returns {Function}
 */
export function throttle(fn, delay = 300) {
  let last = 0
  return function (...args) {
    const now = Date.now()
    if (now - last >= delay) {
      last = now
      fn.apply(this, args)
    }
  }
}

/**
 * 存储封装
 */
export const storage = {
  get(key) {
    const value = localStorage.getItem(key)
    try {
      return JSON.parse(value)
    } catch {
      return value
    }
  },
  set(key, value) {
    if (typeof value === 'object') {
      localStorage.setItem(key, JSON.stringify(value))
    } else {
      localStorage.setItem(key, value)
    }
  },
  remove(key) {
    localStorage.removeItem(key)
  },
  clear() {
    localStorage.clear()
  }
}
