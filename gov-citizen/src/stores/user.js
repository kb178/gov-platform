import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo, loginBySms, sendSmsCode } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')

  // 密码登录
  async function loginAction(loginForm) {
    const { data } = await login(loginForm)
    token.value = data.accessToken
    localStorage.setItem('token', data.accessToken)
    // 直接使用登录接口返回的用户信息
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      userType: data.userType
    }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return data
  }

  // 短信验证码登录
  async function loginBySmsAction(loginForm) {
    const { data } = await loginBySms(loginForm)
    token.value = data.accessToken
    localStorage.setItem('token', data.accessToken)
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      userType: data.userType
    }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return data
  }

  // 发送验证码
  async function sendSmsCodeAction(phone) {
    return await sendSmsCode(phone)
  }

  // 获取用户信息
  async function getUserInfoAction() {
    const { data } = await getUserInfo()
    userInfo.value = data
    return data
  }

  // 登出
  async function logoutAction() {
    try {
      await logout()
    } finally {
      resetState()
    }
  }

  // 重置状态
  function resetState() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    loginAction,
    loginBySmsAction,
    sendSmsCodeAction,
    getUserInfoAction,
    logoutAction,
    resetState
  }
})
