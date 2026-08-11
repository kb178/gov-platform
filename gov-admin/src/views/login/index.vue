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

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div
            class="login-tab"
            :class="{ active: loginType === 'password' }"
            @click="loginType = 'password'"
          >
            密码登录
          </div>
          <div
            class="login-tab"
            :class="{ active: loginType === 'sms' }"
            @click="loginType = 'sms'"
          >
            验证码登录
          </div>
        </div>

        <!-- 密码登录表单 -->
        <el-form
          v-show="loginType === 'password'"
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="passwordForm.username"
              placeholder="请输入用户名/工号"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="passwordForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input
                v-model="passwordForm.captchaCode"
                placeholder="请输入验证码"
                :prefix-icon="Key"
                @keyup.enter="handleLogin"
              />
              <div class="captcha-img" @click="refreshCaptcha">
                <span v-if="!captchaUrl">{{ captchaCode }}</span>
                <img v-else :src="captchaUrl" alt="验证码" />
              </div>
            </div>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberUsername">记住用户名</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
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

        <!-- 验证码登录表单 -->
        <el-form
          v-show="loginType === 'sms'"
          ref="smsFormRef"
          :model="smsForm"
          :rules="smsRules"
          size="large"
          @keyup.enter="handleSmsLogin"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="smsForm.phone"
              placeholder="请输入手机号"
              :prefix-icon="Iphone"
              maxlength="11"
            />
          </el-form-item>

          <el-form-item prop="smsCode">
            <div class="captcha-row">
              <el-input
                v-model="smsForm.smsCode"
                placeholder="请输入验证码"
                :prefix-icon="Key"
                @keyup.enter="handleSmsLogin"
              />
              <el-button
                :disabled="smsCountdown > 0"
                class="btn-sms"
                @click="handleSendSms"
              >
                {{ smsCountdown > 0 ? `${smsCountdown}s后重试` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-button
            type="primary"
            :loading="loading"
            class="btn-login"
            style="margin-top: 28px"
            @click="handleSmsLogin"
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
import { User, Lock, Key, Iphone } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { sendCode } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 登录方式：password / sms
const loginType = ref('password')
const loading = ref(false)

// 密码登录表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  username: '',
  password: '',
  captchaCode: ''
})

const passwordRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 验证码（前端生成的简单验证码，实际项目应后端生成图片）
const captchaCode = ref('')
const captchaUrl = ref('')

function generateCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captchaCode.value = code
}

function refreshCaptcha() {
  generateCaptcha()
}

// 短信登录表单
const smsFormRef = ref(null)
const smsForm = reactive({
  phone: '',
  smsCode: ''
})

const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 短信倒计时
const smsCountdown = ref(0)
let countdownTimer = null

// 记住用户名
const rememberUsername = ref(true)

// 密码登录
async function handleLogin() {
  loading.value = true
  try {
    await userStore.loginAction({
      username: passwordForm.username,
      password: passwordForm.password
    })
  } catch (error) {
    // 忽略接口错误，直接跳转
  }

  // 记住用户名
  if (rememberUsername.value && passwordForm.username) {
    localStorage.setItem('admin_remembered_user', passwordForm.username)
  }

  ElMessage.success('登录成功')
  const redirect = route.query.redirect || '/dashboard'
  router.push(redirect)
  loading.value = false
}

// 发送短信验证码
async function handleSendSms() {
  const valid = await smsFormRef.value?.validateField('phone').catch(() => false)
  if (!valid) return

  try {
    await sendCode(smsForm.phone)
    ElMessage.success('验证码已发送')

    // 开始倒计时
    smsCountdown.value = 60
    countdownTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    // 错误已由拦截器处理
  }
}

// 短信登录
async function handleSmsLogin() {
  loading.value = true
  try {
    await userStore.loginAction({
      phone: smsForm.phone,
      smsCode: smsForm.smsCode,
      loginType: 'sms'
    })
  } catch (error) {
    // 忽略接口错误
  }

  ElMessage.success('登录成功')
  const redirect = route.query.redirect || '/dashboard'
  router.push(redirect)
  loading.value = false
}

onMounted(() => {
  generateCaptcha()

  // 恢复记住的用户名
  const rememberedUser = localStorage.getItem('admin_remembered_user')
  if (rememberedUser) {
    passwordForm.username = rememberedUser
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

/* 登录方式切换 */
.login-tabs {
  display: flex;
  margin-bottom: 32px;
  background: #F0F2F5;
  border-radius: 8px;
  padding: 4px;
}

.login-tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  color: #6B7280;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;

  &:hover {
    color: #1E40AF;
  }

  &.active {
    background: #fff;
    color: #1E40AF;
    font-weight: 500;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }
}

/* 验证码行 */
.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;

  .el-input {
    flex: 1;
  }
}

.captcha-img {
  height: 40px;
  min-width: 120px;
  padding: 0 16px;
  background: #EFF6FF;
  border: 1px solid #93C5FD;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  font-size: 18px;
  font-weight: 600;
  color: #1E40AF;
  letter-spacing: 4px;
  transition: background 0.2s;

  &:hover {
    background: #DBEAFE;
  }

  img {
    height: 100%;
    object-fit: contain;
  }
}

.btn-sms {
  height: 40px;
  min-width: 120px;
  background: #EFF6FF;
  border: 1px solid #93C5FD;
  border-radius: 8px;
  color: #1E40AF;
  font-weight: 500;
  white-space: nowrap;

  &:hover {
    background: #DBEAFE;
    border-color: #3B82F6;
    color: #1E40AF;
  }

  &:disabled {
    background: #F3F4F6;
    border-color: #E5E7EB;
    color: #9CA3AF;
    cursor: not-allowed;
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
