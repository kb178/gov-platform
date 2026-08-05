<template>
  <div class="login-page">
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">🏛️</div>
        <h1 class="brand-title">海口政务服务</h1>
        <p class="brand-subtitle">让数据多跑路，让群众少跑腿</p>

        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-icon">📱</div>
            <div class="feature-text">掌上办事</div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">⚡</div>
            <div class="feature-text">高效审批</div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🔒</div>
            <div class="feature-text">安全可靠</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-form-wrapper">
      <div class="login-form">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>登录后享受更多政务服务</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div
            :class="['login-tab', { active: loginType === 'password' }]"
            @click="loginType = 'password'"
          >
            密码登录
          </div>
          <div
            :class="['login-tab', { active: loginType === 'sms' }]"
            @click="loginType = 'sms'"
          >
            验证码登录
          </div>
        </div>

        <!-- 密码登录 -->
        <el-form
          v-show="loginType === 'password'"
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          @keyup.enter="handlePasswordLogin"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="passwordForm.phone"
              placeholder="请输入手机号"
              :prefix-icon="Iphone"
              maxlength="11"
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

          <div class="form-options">
            <el-checkbox v-model="passwordForm.remember">记住密码</el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
          </div>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handlePasswordLogin"
          >
            登 录
          </el-button>
        </el-form>

        <!-- 验证码登录 -->
        <el-form
          v-show="loginType === 'sms'"
          ref="smsFormRef"
          :model="smsForm"
          :rules="smsRules"
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

          <el-form-item prop="code">
            <el-input
              v-model="smsForm.code"
              placeholder="请输入验证码"
              :prefix-icon="ChatDotRound"
              maxlength="6"
            >
              <template #append>
                <el-button
                  :disabled="countdown > 0"
                  @click="handleSendSms"
                >
                  {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="smsForm.remember">记住登录状态</el-checkbox>
          </div>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleSmsLogin"
          >
            登 录
          </el-button>
        </el-form>

        <!-- 注册链接 -->
        <div class="form-links">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>

        <!-- 第三方登录 -->
        <div class="divider">
          <span>其他登录方式</span>
        </div>

        <div class="third-party-login">
          <el-tooltip content="微信登录" placement="top">
            <button class="third-party-btn wechat" @click="handleThirdPartyLogin('wechat')">
              <el-icon :size="24"><ChatDotRound /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="支付宝登录" placement="top">
            <button class="third-party-btn alipay" @click="handleThirdPartyLogin('alipay')">
              <el-icon :size="24"><Wallet /></el-icon>
            </button>
          </el-tooltip>
        </div>

        <!-- 用户协议 -->
        <div class="agreement">
          登录即表示同意
          <a href="#">《用户服务协议》</a>
          和
          <a href="#">《隐私政策》</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, ChatDotRound, Wallet } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 登录方式
const loginType = ref('password')
const loading = ref(false)

// 密码登录表单
const passwordFormRef = ref()
const passwordForm = reactive({
  phone: '',
  password: '',
  remember: true
})

// 验证码登录表单
const smsFormRef = ref()
const smsForm = reactive({
  phone: '',
  code: '',
  remember: true
})

// 手机号验证
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 密码登录校验规则
const passwordRules = {
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 验证码登录校验规则
const smsRules = {
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 验证码倒计时
const countdown = ref(0)
let countdownTimer = null

onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

// 发送验证码
const handleSendSms = async () => {
  try {
    await smsFormRef.value.validateField('phone')
    // TODO: 调用发送验证码接口
    // await userStore.sendSmsCode(smsForm.phone)

    ElMessage.success('验证码已发送')
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    // 验证失败
  }
}

// 密码登录
const handlePasswordLogin = async () => {
  try {
    await passwordFormRef.value.validate()
    loading.value = true

    // TODO: 调用密码登录接口
    // await userStore.loginByPassword({
    //   phone: passwordForm.phone,
    //   password: passwordForm.password
    // })

    // 模拟登录成功
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 验证码登录
const handleSmsLogin = async () => {
  try {
    await smsFormRef.value.validate()
    loading.value = true

    // TODO: 调用验证码登录接口
    // await userStore.loginBySms({
    //   phone: smsForm.phone,
    //   code: smsForm.code
    // })

    // 模拟登录成功
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 第三方登录
const handleThirdPartyLogin = (type) => {
  ElMessage.info(`${type === 'wechat' ? '微信' : '支付宝'}登录功能开发中`)
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #1E40AF 0%, #3B82F6 50%, #60A5FA 100%);
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  color: white;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 60%);
    animation: float 15s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -30px); }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.brand-logo {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 40px;
  backdrop-filter: blur(10px);
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
}

.brand-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 48px;
}

.brand-features {
  display: flex;
  gap: 48px;
}

.feature-item {
  text-align: center;
}

.feature-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  font-size: 24px;
}

.feature-text {
  font-size: 14px;
  opacity: 0.9;
}

/* 右侧登录表单 */
.login-form-wrapper {
  width: 520px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.login-form {
  width: 100%;
  max-width: 400px;
}

.login-header {
  margin-bottom: 40px;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  p {
    color: var(--text-secondary);
    font-size: 15px;
  }
}

/* 登录方式切换 */
.login-tabs {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-light);
  padding-bottom: 16px;
}

.login-tab {
  font-size: 15px;
  color: var(--text-secondary);
  cursor: pointer;
  padding-bottom: 16px;
  position: relative;
  transition: color 0.2s;

  &:hover {
    color: var(--text-primary);
  }

  &.active {
    color: var(--primary);
    font-weight: 600;

    &::after {
      content: '';
      position: absolute;
      bottom: -17px;
      left: 0;
      right: 0;
      height: 2px;
      background: var(--primary);
    }
  }
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.forgot-link {
  font-size: 14px;
  color: var(--text-secondary);

  &:hover {
    color: var(--primary);
  }
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

/* 注册链接 */
.form-links {
  display: flex;
  gap: 16px;
  margin-top: 24px;
  justify-content: center;
  font-size: 14px;
  color: var(--text-secondary);

  a {
    color: var(--primary-light);

    &:hover {
      color: var(--primary);
    }
  }
}

/* 第三方登录 */
.divider {
  display: flex;
  align-items: center;
  margin: 32px 0;
  color: var(--text-placeholder);
  font-size: 13px;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--border-light);
  }

  span {
    padding: 0 16px;
  }
}

.third-party-login {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.third-party-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  background: white;

  &:hover {
    border-color: var(--primary-lighter);
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  &.wechat { color: #07C160; }
  &.alipay { color: #1677FF; }
}

/* 用户协议 */
.agreement {
  margin-top: 32px;
  text-align: center;
  font-size: 12px;
  color: var(--text-secondary);

  a {
    color: var(--primary-light);

    &:hover {
      color: var(--primary);
    }
  }
}

/* 响应式 */
@media (max-width: 1024px) {
  .login-brand {
    display: none;
  }

  .login-form-wrapper {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-form-wrapper {
    padding: 40px 24px;
  }

  .brand-features {
    flex-direction: column;
    gap: 24px;
  }
}
</style>
