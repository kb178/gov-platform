<template>
  <div class="login-page">
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">🏛️</div>
        <h1 class="brand-title">海口政务服务</h1>
        <p class="brand-subtitle">管理后台系统</p>

        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <div class="feature-text">数据统计</div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📋</div>
            <div class="feature-text">审批管理</div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">⚙️</div>
            <div class="feature-text">系统配置</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="login-section">
      <div class="login-container">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>请输入您的账号信息登录系统</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberUsername">记住用户名</el-checkbox>
          </div>

          <el-button
            type="primary"
            :loading="loading"
            class="btn-login"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>
      </div>

      <div class="login-footer">
        <p>© 2024 海口市政务服务平台 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)

// 登录表单
const loginFormRef = ref(null)
const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 记住用户名
const rememberUsername = ref(true)

// 登录
async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate()

  loading.value = true
  try {
    await userStore.loginAction({
      username: loginForm.username,
      password: loginForm.password
    })

    // 记住用户名
    if (rememberUsername.value && loginForm.username) {
      localStorage.setItem('admin_remembered_username', loginForm.username)
    }

    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (error) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 恢复记住的用户名
  const rememberedUsername = localStorage.getItem('admin_remembered_username')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
  }
})
</script>

<style lang="scss" scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  font-family: system-ui, -apple-system, 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', sans-serif;
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, #1E3A8A 0%, #1E40AF 50%, #3B82F6 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
    animation: pulse 8s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -20%;
    right: -20%;
    width: 60%;
    height: 60%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
    border-radius: 50%;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.brand-logo {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin: 0 auto 32px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: 2px;
}

.brand-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 48px;
  font-weight: 300;
}

.brand-features {
  display: flex;
  gap: 48px;
  margin-top: 60px;
}

.feature-item {
  text-align: center;
}

.feature-icon {
  font-size: 32px;
  margin-bottom: 12px;
  opacity: 0.9;
}

.feature-text {
  font-size: 14px;
  opacity: 0.8;
}

/* 右侧登录区域 */
.login-section {
  width: 520px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: #fff;
}

.login-container {
  width: 100%;
  max-width: 380px;
}

.login-header {
  margin-bottom: 40px;

  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #1F2937;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #6B7280;
  }
}

/* 表单选项 */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

/* 登录按钮 */
.btn-login {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: #1E40AF;
  border-color: #1E40AF;
  transition: all 0.2s;

  &:hover {
    background: #3B82F6;
    border-color: #3B82F6;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(30, 64, 175, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
}

/* 页脚 */
.login-footer {
  position: absolute;
  bottom: 24px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 12px;
  color: #9CA3AF;
}

/* 响应式 */
@media (max-width: 1024px) {
  .login-brand {
    display: none;
  }

  .login-section {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-section {
    padding: 40px 24px;
  }
}
</style>
