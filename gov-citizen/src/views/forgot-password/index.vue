<template>
  <div class="forgot-page">
    <div class="forgot-card">
      <div class="forgot-header">
        <div class="icon">🔑</div>
        <h2>找回密码</h2>
        <p>通过手机号验证重置密码</p>
      </div>

      <div class="forgot-body">
        <!-- 步骤指示器 -->
        <div class="steps">
          <div :class="['step-item', { completed: currentStep > 0, active: currentStep === 0 }]">
            <div class="step-circle">{{ currentStep > 0 ? '✓' : '1' }}</div>
            <div class="step-label">验证手机</div>
          </div>
          <div :class="['step-item', { completed: currentStep > 1, active: currentStep === 1 }]">
            <div class="step-circle">{{ currentStep > 1 ? '✓' : '2' }}</div>
            <div class="step-label">设置密码</div>
          </div>
          <div :class="['step-item', { active: currentStep === 2 }]">
            <div class="step-circle">3</div>
            <div class="step-label">完成</div>
          </div>
        </div>

        <!-- 第一步：验证手机 -->
        <div v-if="currentStep === 0" class="form-section">
          <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules">
            <el-form-item prop="phone">
              <el-input
                v-model="phoneForm.phone"
                placeholder="请输入注册时的手机号"
                maxlength="11"
                size="large"
              >
                <template #prefix>📱</template>
              </el-input>
            </el-form-item>

            <el-form-item prop="smsCode">
              <div class="sms-input-group">
                <el-input
                  v-model="phoneForm.smsCode"
                  placeholder="请输入验证码"
                  maxlength="6"
                  size="large"
                >
                  <template #prefix>💬</template>
                </el-input>
                <el-button class="sms-btn" :disabled="countdown > 0" @click="handleSendSms">
                  {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
          </el-form>

          <el-button type="primary" size="large" class="btn-block" @click="handleVerifyPhone">
            下一步
          </el-button>

          <div class="form-links">
            <router-link to="/login">返回登录</router-link>
          </div>
        </div>

        <!-- 第二步：设置密码 -->
        <div v-if="currentStep === 1" class="form-section">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules">
            <el-form-item prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请设置8-20位新密码"
                size="large"
                show-password
                @input="checkPasswordStrength"
              >
                <template #prefix>🔒</template>
              </el-input>
              <div class="password-strength">
                <div
                  :class="['strength-bar', { active: passwordStrength >= 1 }, strengthLevel]"
                ></div>
                <div
                  :class="['strength-bar', { active: passwordStrength >= 2 }, strengthLevel]"
                ></div>
                <div
                  :class="['strength-bar', { active: passwordStrength >= 3 }, strengthLevel]"
                ></div>
              </div>
              <div class="form-hint">密码需包含字母和数字，长度8-20位</div>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                size="large"
                show-password
              >
                <template #prefix>🔒</template>
              </el-input>
            </el-form-item>
          </el-form>

          <el-button
            type="primary"
            size="large"
            class="btn-block"
            :loading="submitting"
            @click="handleResetPassword"
          >
            重置密码
          </el-button>
        </div>

        <!-- 第三步：完成 -->
        <div v-if="currentStep === 2" class="form-section">
          <div class="success-view">
            <div class="success-icon">✅</div>
            <div class="success-title">密码重置成功！</div>
            <div class="success-desc">您的密码已成功重置，请使用新密码登录</div>
            <div class="success-actions">
              <el-button type="primary" size="large" @click="router.push('/login')">
                立即登录
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 步骤
const currentStep = ref(0)

// 第一步：验证手机
const phoneFormRef = ref()
const phoneForm = reactive({
  phone: '',
  smsCode: ''
})

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位', trigger: 'blur' }
  ]
}

// 验证码倒计时
const countdown = ref(0)
let timer = null

const handleSendSms = () => {
  if (!phoneForm.phone || !/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }

  countdown.value = 60
  ElMessage.success('验证码已发送到您的手机')

  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleVerifyPhone = async () => {
  try {
    await phoneFormRef.value.validate()
    currentStep.value = 1
  } catch (error) {
    console.error('验证失败:', error)
  }
}

// 第二步：设置密码
const passwordFormRef = ref()
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度为8-20位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

// 密码强度
const passwordStrength = ref(0)
const strengthLevel = computed(() => {
  if (passwordStrength.value <= 1) return 'weak'
  if (passwordStrength.value <= 2) return 'medium'
  return 'strong'
})

const checkPasswordStrength = () => {
  const password = passwordForm.newPassword
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

// 提交状态
const submitting = ref(false)

const handleResetPassword = async () => {
  try {
    await passwordFormRef.value.validate()
    submitting.value = true

    // 模拟重置密码请求
    await new Promise(resolve => setTimeout(resolve, 1500))

    currentStep.value = 2
    ElMessage.success('密码重置成功')
  } catch (error) {
    console.error('重置失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.forgot-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  padding: 20px;
}

.forgot-card {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.forgot-header {
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  padding: 40px;
  text-align: center;
  color: white;

  .icon {
    width: 64px;
    height: 64px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    font-size: 32px;
  }

  h2 {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    opacity: 0.9;
  }
}

.forgot-body {
  padding: 40px;
}

/* 步骤指示器 */
.steps {
  display: flex;
  justify-content: center;
  gap: 0;
  margin-bottom: 32px;
}

.step-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    top: 16px;
    left: 50%;
    width: 100%;
    height: 2px;
    background: #e5e7eb;
  }

  &.active:not(:last-child)::after,
  &.completed:not(:last-child)::after {
    background: #1e40af;
  }
}

.step-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  color: #6b7280;
  position: relative;
  z-index: 1;
}

.step-item.active .step-circle {
  background: #1e40af;
  color: white;
}

.step-item.completed .step-circle {
  background: #10b981;
  color: white;
}

.step-label {
  margin-top: 8px;
  font-size: 12px;
  color: #6b7280;
}

.step-item.active .step-label {
  color: #1e40af;
  font-weight: 500;
}

.step-item.completed .step-label {
  color: #10b981;
}

/* 表单 */
.form-section {
  min-height: 200px;
}

.sms-input-group {
  display: flex;
  gap: 12px;
  width: 100%;

  .el-input {
    flex: 1;
  }

  .sms-btn {
    width: 120px;
    flex-shrink: 0;
  }
}

.form-hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 8px;
}

.form-links {
  text-align: center;
  margin-top: 24px;

  a {
    color: #3b82f6;
    font-size: 14px;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.btn-block {
  width: 100%;
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
  background: #e5e7eb;
  border-radius: 2px;
  transition: background 0.3s;

  &.active.weak {
    background: #ef4444;
  }
  &.active.medium {
    background: #f59e0b;
  }
  &.active.strong {
    background: #10b981;
  }
}

/* 成功页面 */
.success-view {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 80px;
  margin-bottom: 24px;
}

.success-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.success-desc {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 32px;
}

.success-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 480px) {
  .forgot-body {
    padding: 24px;
  }

  .steps {
    flex-direction: column;
    gap: 16px;
  }

  .step-item:not(:last-child)::after {
    display: none;
  }

  .sms-input-group {
    flex-direction: column;

    .sms-btn {
      width: 100%;
    }
  }
}
</style>
