import { describe, it, expect } from 'vitest'
import { formatDate, maskPhone, maskIdCard, debounce } from './index'

describe('formatDate', () => {
  it('应该正确格式化日期', () => {
    const date = new Date(2024, 0, 15, 16, 30, 0) // 2024-01-15 16:30:00
    const result = formatDate(date, 'YYYY-MM-DD HH:mm:ss')
    expect(result).toBe('2024-01-15 16:30:00')
  })

  it('应该支持自定义格式', () => {
    const date = new Date(2024, 0, 15, 16, 30, 0)
    const result = formatDate(date, 'YYYY/MM/DD')
    expect(result).toBe('2024/01/15')
  })

  it('空值应该返回空字符串', () => {
    expect(formatDate(null)).toBe('')
    expect(formatDate(undefined)).toBe('')
    expect(formatDate('')).toBe('')
  })
})

describe('maskPhone', () => {
  it('应该正确脱敏手机号', () => {
    expect(maskPhone('13812345678')).toBe('138****5678')
  })

  it('空值应该返回空字符串', () => {
    expect(maskPhone('')).toBe('')
    expect(maskPhone(null)).toBe('')
  })
})

describe('maskIdCard', () => {
  it('应该正确脱敏身份证号', () => {
    expect(maskIdCard('460100199001011234')).toBe('4601**********1234')
  })

  it('空值应该返回空字符串', () => {
    expect(maskIdCard('')).toBe('')
    expect(maskIdCard(null)).toBe('')
  })
})

describe('debounce', () => {
  it('应该延迟执行函数', async () => {
    let count = 0
    const fn = debounce(() => {
      count++
    }, 100)

    fn()
    fn()
    fn()

    expect(count).toBe(0)

    await new Promise(resolve => setTimeout(resolve, 150))
    expect(count).toBe(1)
  })
})
