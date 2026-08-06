import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(null)
  const roles = ref([])
  const permissions = ref([])

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // 登录
  async function loginAction(loginForm) {
    const { data } = await login(loginForm)
    token.value = data.token
    localStorage.setItem('admin_token', data.token)
    await getUserInfoAction()
    return data
  }

  // 获取用户信息
  async function getUserInfoAction() {
    const { data } = await getUserInfo()
    userInfo.value = data.user
    roles.value = data.roles || []
    permissions.value = data.permissions || []
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
    roles.value = []
    permissions.value = []
    localStorage.removeItem('admin_token')
  }

  // 检查权限
  function hasPermission(permission) {
    return permissions.value.includes('*:*:*') || permissions.value.includes(permission)
  }

  // 检查角色
  function hasRole(role) {
    return roles.value.includes('admin') || roles.value.includes(role)
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    isLoggedIn,
    username,
    nickname,
    avatar,
    loginAction,
    getUserInfoAction,
    logoutAction,
    resetState,
    hasPermission,
    hasRole
  }
})
