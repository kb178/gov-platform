<template>
  <div class="register-page">
    <!-- 左侧品牌区域 -->
    <div class="register-brand">
      <div class="brand-content">
        <div class="brand-logo">🏛️</div>
        <h1 class="brand-title">海口政务服务</h1>
        <p class="brand-subtitle">注册账号，享受便捷政务服务</p>

        <div class="brand-steps">
          <h3>注册流程</h3>
          <div class="step-item">
            <div class="step-number">1</div>
            <div class="step-text">填写手机号获取验证码</div>
          </div>
          <div class="step-item">
            <div class="step-number">2</div>
            <div class="step-text">设置登录密码</div>
          </div>
          <div class="step-item">
            <div class="step-number">3</div>
            <div class="step-text">注册成功，开始使用</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单 -->
    <div class="register-form-wrapper">
      <div class="register-form">
        <div class="register-header">
          <h2>用户注册</h2>
          <p>注册后即可在线办理政务服务</p>
        </div>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          @keyup.enter="handleRegister"
        >
          <!-- 手机号 -->
          <el-form-item prop="phone">
            <el-input
              v-model="formData.phone"
              placeholder="请输入手机号"
              :prefix-icon="Iphone"
              maxlength="11"
            />
          </el-form-item>

          <!-- 验证码 -->
          <el-form-item prop="code">
            <el-input
              v-model="formData.code"
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

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="formData.password"
              type="password"
              placeholder="请设置8-20位密码"
              :prefix-icon="Lock"
              show-password
              @input="checkPasswordStrength"
            />
            <div class="password-strength">
              <div :class="['strength-bar', { active: passwordStrength >= 1 }, strengthLevel]"></div>
              <div :class="['strength-bar', { active: passwordStrength >= 2 }, strengthLevel]"></div>
              <div :class="['strength-bar', { active: passwordStrength >= 3 }, strengthLevel]"></div>
            </div>
            <div :class="['strength-text', strengthLevel]" v-if="formData.password">
              密码强度：{{ strengthText }}
            </div>
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="formData.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <!-- 用户协议 -->
          <el-form-item prop="agreement">
            <el-checkbox v-model="formData.agreement">
              我已阅读并同意
              <a href="#" class="agreement-link">《用户服务协议》</a>
              和
              <a href="#" class="agreement-link">《隐私政策》</a>
            </el-checkbox>
          </el-form-item>

          <!-- 注册按钮 -->
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form>

        <!-- 登录链接 -->
        <div class="form-links">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, ChatDotRound } from '@element-plus/icons-vue'
import { sendSmsCode, register } from '@/api/user'

const router = useRouter()

const loading = ref(false)
const formRef = ref()

// 表单数据
const formData = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
  agreement: false
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

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 协议验证
const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请同意用户服务协议和隐私政策'))
  } else {
    callback()
  }
}

// 表单校验规则
const rules = {
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度为8-20位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  agreement: [{ validator: validateAgreement, trigger: 'change' }]
}

// 密码强度
const passwordStrength = ref(0)
const strengthLevel = computed(() => {
  if (passwordStrength.value <= 1) return 'weak'
  if (passwordStrength.value <= 2) return 'medium'
  return 'strong'
})

const strengthText = computed(() => {
  if (passwordStrength.value <= 1) return '弱'
  if (passwordStrength.value <= 2) return '中'
  return '强'
})

// 检查密码强度
const checkPasswordStrength = () => {
  const password = formData.password
  if (!password) {
    passwordStrength.value = 0
    return
  }

  let strength = 0
  if (password.length >= 8) strength++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[^a-zA-Z0-9]/.test(password)) strength++

  passwordStrength.value = Math.min(strength, 3)
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
    await formRef.value.validateField('phone')
    await sendSmsCode(formData.phone)

    ElMessage.success('验证码已发送')
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

// 注册
const handleRegister = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    await register({
      phone: formData.phone,
      code: formData.code,
      password: formData.password,
      confirmPassword: formData.confirmPassword
    })

    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #1E40AF 0%, #3B82F6 50%, #60A5FA 100%);
}

/* 左侧品牌区域 */
.register-brand {
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

.brand-steps {
  text-align: left;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 32px;
  backdrop-filter: blur(10px);

  h3 {
    font-size: 18px;
    margin-bottom: 24px;
    text-align: center;
  }
}

.step-item {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.step-number {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.step-text {
  font-size: 14px;
  opacity: 0.9;
}

/* 右侧注册表单 */
.register-form-wrapper {
  width: 520px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.register-form {
  width: 100%;
  max-width: 400px;
}

.register-header {
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

/* 密码强度 */
.password-strength {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: var(--border-light);
  border-radius: 2px;
  transition: background 0.3s;

  &.active.weak { background: var(--danger); }
  &.active.medium { background: var(--warning); }
  &.active.strong { background: var(--success); }
}

.strength-text {
  font-size: 12px;
  margin-top: 4px;

  &.weak { color: var(--danger); }
  &.medium { color: var(--warning); }
  &.strong { color: var(--success); }
}

/* 用户协议链接 */
.agreement-link {
  color: var(--primary-light);

  &:hover {
    color: var(--primary);
  }
}

/* 注册按钮 */
.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
}

/* 登录链接 */
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

/* 响应式 */
@media (max-width: 1024px) {
  .register-brand {
    display: none;
  }

  .register-form-wrapper {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .register-form-wrapper {
    padding: 40px 24px;
  }
}
</style>
