import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
        //发请求带上token
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const { code, msg, data } = response.data

    // 成功响应
    if (code === 200) {
      return response.data
    }

    // token 过期
    if (code === 401) {
      const userStore = useUserStore()
      userStore.resetState()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(new Error(msg))
    }

    // 其他错误
    ElMessage.error(msg || '请求失败')
    return Promise.reject(new Error(msg))
  },
  (error) => {
    // HTTP 错误
    console.error('请求错误详情:', error)
    const { status, data } = error.response || {}
    const messages = {
      400: '请求参数错误',
      401: '未授权，请登录',
      403: '拒绝访问',
      404: '请求地址不存在',
      500: '服务器内部错误'
    }
    const errorMsg = data?.msg || messages[status] || '网络连接异常'
    ElMessage.error(errorMsg)
    return Promise.reject(error)
  }
)

export default service
