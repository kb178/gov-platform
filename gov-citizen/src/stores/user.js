import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')

  // 登录
  async function loginAction(loginForm) {
    const { data } = await login(loginForm)
    token.value = data.token
    localStorage.setItem('token', data.token)
    await getUserInfoAction()
    return data
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
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    loginAction,
    getUserInfoAction,
    logoutAction,
    resetState
  }
})
