import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { adminLogin, getUserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(null)
  const roles = ref([])
  const permissions = ref([])

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.userName || '')
  const nickname = computed(() => userInfo.value?.nickName || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // 管理员登录（用户名+密码）
  async function loginAction(loginForm) {
    try {
      const { data } = await adminLogin(loginForm)

      // 保存token
      token.value = data.accessToken
      localStorage.setItem('admin_token', data.accessToken)

      // 保存基本用户信息
      userInfo.value = {
        userId: data.userId,
        userName: data.username,
        nickName: data.nickname
      }

      return data
    } catch (error) {
      // 登录失败，清除状态
      resetState()
      throw error
    }
  }

  // 获取用户信息
  async function getUserInfoAction() {
    try {
      const res = await getUserInfo()
      const data = res.data

      userInfo.value = {
        userId: data.userId,
        userName: data.userName,
        nickName: data.nickName,
        avatar: data.avatar,
        phone: data.phone,
        email: data.email,
        sex: data.sex,
        deptId: data.deptId,
        deptName: data.deptName,
        userType: data.userType
      }

      roles.value = data.roles || []
      permissions.value = data.permissions || []

      return data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取失败，使用基础信息
      if (!userInfo.value) {
        userInfo.value = {
          userName: 'admin',
          nickName: '用户'
        }
      }
      roles.value = ['admin']
      permissions.value = ['*:*:*']
    }
  }

  // 登出
  async function logoutAction() {
    resetState()
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
