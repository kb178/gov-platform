<template>
  <div class="verify-page">
    <div class="page-container">
      <div class="auth-card">
        <div class="auth-header">
          <div class="auth-icon">🆔</div>
          <div class="auth-title">实名认证</div>
          <div class="auth-subtitle">认证后可享受完整的政务服务功能</div>
        </div>

        <div class="auth-body">
          <!-- 认证前 - 表单 -->
          <template v-if="!authSuccess">
            <!-- 步骤指示器 -->
            <div class="steps">
              <div
                :class="['step-item', { completed: currentStep > 0, active: currentStep === 0 }]"
              >
                <div class="step-circle">{{ currentStep > 0 ? '✓' : '1' }}</div>
                <div class="step-label">填写信息</div>
              </div>
              <div
                :class="['step-item', { completed: currentStep > 1, active: currentStep === 1 }]"
              >
                <div class="step-circle">{{ currentStep > 1 ? '✓' : '2' }}</div>
                <div class="step-label">身份验证</div>
              </div>
              <div :class="['step-item', { active: currentStep === 2 }]">
                <div class="step-circle">3</div>
                <div class="step-label">认证完成</div>
              </div>
            </div>

            <!-- 步骤1：填写信息 -->
            <el-form
              v-if="currentStep === 0"
              ref="formRef"
              :model="authForm"
              :rules="authRules"
              class="auth-form"
              label-position="top"
            >
              <el-form-item label="真实姓名" prop="realName">
                <el-input
                  v-model="authForm.realName"
                  placeholder="请输入您的真实姓名"
                  size="large"
                />
              </el-form-item>

              <el-form-item label="身份证号码" prop="idCard">
                <el-input
                  v-model="authForm.idCard"
                  placeholder="请输入18位身份证号码"
                  maxlength="18"
                  size="large"
                />
                <div class="form-hint">请确保信息准确，认证后不可修改</div>
              </el-form-item>

              <div class="auth-agreement">
                <el-checkbox v-model="authForm.agreed">
                  我已阅读并同意
                  <a href="#" @click.prevent>《实名认证服务协议》</a>
                  和
                  <a href="#" @click.prevent>《个人信息保护政策》</a>
                </el-checkbox>
              </div>

              <div class="auth-actions">
                <el-button
                  type="primary"
                  size="large"
                  class="btn-auth"
                  :loading="submitting"
                  @click="handleNext"
                >
                  下一步
                </el-button>
              </div>
            </el-form>

            <!-- 步骤2：身份验证 -->
            <div v-if="currentStep === 1" class="verify-step">
              <div class="verify-info-card">
                <div class="verify-info-title">请确认以下信息</div>
                <div class="verify-info-list">
                  <div class="verify-info-item">
                    <span class="label">姓名</span>
                    <span class="value">{{ authForm.realName }}</span>
                  </div>
                  <div class="verify-info-item">
                    <span class="label">身份证号</span>
                    <span class="value">{{ authForm.idCard }}</span>
                  </div>
                </div>
              </div>

              <div class="verify-notice">
                <div class="notice-icon">⚠️</div>
                <div class="notice-content">
                  <div class="notice-title">身份核验提示</div>
                  <div class="notice-desc">
                    系统将调用公安部身份核验接口进行实名校验，请确保填写的信息与身份证一致。
                  </div>
                </div>
              </div>

              <div class="auth-actions">
                <el-button size="large" @click="currentStep = 0">上一步</el-button>
                <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
                  确认认证
                </el-button>
              </div>
            </div>

            <!-- 认证提示 -->
            <div v-if="currentStep === 0" class="auth-tips">
              <h4>💡 认证提示</h4>
              <ul>
                <li>认证信息将严格保密，仅用于政务服务办理</li>
                <li>认证通过后，姓名和身份证号不可修改</li>
                <li>如信息有误，请联系客服 0898-12345</li>
                <li>认证过程中遇到问题可查看帮助中心</li>
              </ul>
            </div>
          </template>

          <!-- 认证成功 -->
          <template v-else>
            <div class="auth-success">
              <div class="success-icon">✅</div>
              <div class="success-title">实名认证成功！</div>
              <div class="success-desc">您已完成实名认证，可以享受完整的政务服务功能</div>

              <div class="success-info">
                <div class="success-info-item">
                  <span class="label">姓名</span>
                  <span class="value">{{ authForm.realName }}</span>
                </div>
                <div class="success-info-item">
                  <span class="label">身份证</span>
                  <span class="value">{{ maskIdCard(authForm.idCard) }}</span>
                </div>
                <div class="success-info-item">
                  <span class="label">认证时间</span>
                  <span class="value">{{ verifyTime }}</span>
                </div>
              </div>

              <div class="success-actions">
                <el-button type="primary" size="large" @click="router.push('/')">
                  返回首页
                </el-button>
                <el-button size="large" @click="router.push('/profile')"> 查看个人信息 </el-button>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { realNameAuth, getRealNameStatus } from '@/api/user'

const router = useRouter()

// 步骤
const currentStep = ref(0)

// 表单
const formRef = ref()
const authForm = reactive({
  realName: '',
  idCard: '',
  agreed: false
})

const authRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号码', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的身份证号码', trigger: 'blur' }
  ]
}

// 提交状态
const submitting = ref(false)

// 认证成功
const authSuccess = ref(false)
const verifyTime = ref('')

// 下一步
const handleNext = async () => {
  try {
    await formRef.value.validate()

    if (!authForm.agreed) {
      ElMessage.warning('请阅读并同意实名认证服务协议')
      return
    }

    currentStep.value = 1
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 提交认证
const handleSubmit = async () => {
  submitting.value = true

  try {
    await realNameAuth({
      realName: authForm.realName,
      idCard: authForm.idCard
    })

    verifyTime.value = new Date().toLocaleString('zh-CN')
    authSuccess.value = true
    ElMessage.success('实名认证成功')
  } catch (error) {
    console.error('认证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 查询认证状态
const checkAuthStatus = async () => {
  try {
    const { data } = await getRealNameStatus()
    if (data && data.status === 1) {
      // 已认证，显示认证成功页面
      authForm.realName = data.realName || ''
      authForm.idCard = data.idCard || ''
      verifyTime.value = data.verifyTime || ''
      authSuccess.value = true
    }
  } catch (error) {
    console.error('查询认证状态失败:', error)
  }
}

// 页面加载时检查认证状态
onMounted(() => {
  checkAuthStatus()
})

// 数据脱敏
const maskIdCard = idCard => {
  if (!idCard) return '-'
  return idCard.replace(/^(.{6})(.*)(.{4})$/, '$1****$3')
}
</script>

<style lang="scss" scoped>
.verify-page {
  min-height: 100vh;
  background: #f3f4f6;
}

.page-container {
  padding: 40px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.auth-card {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.auth-header {
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  padding: 40px;
  text-align: center;
  color: white;
}

.auth-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 40px;
}

.auth-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.auth-subtitle {
  font-size: 14px;
  opacity: 0.9;
}

.auth-body {
  padding: 40px;
}

/* 步骤指示器 */
.steps {
  display: flex;
  justify-content: center;
  gap: 0;
  margin-bottom: 40px;
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
    top: 20px;
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
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
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
  margin-top: 12px;
  font-size: 13px;
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
.auth-form {
  max-width: 400px;
  margin: 0 auto;
}

.form-hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 8px;
}

.auth-agreement {
  margin-bottom: 32px;

  a {
    color: #3b82f6;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.auth-actions {
  text-align: center;
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn-auth {
  width: 200px;
}

/* 身份验证步骤 */
.verify-step {
  max-width: 400px;
  margin: 0 auto;
}

.verify-info-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.verify-info-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.verify-info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.verify-info-item {
  display: flex;
  font-size: 14px;

  .label {
    width: 80px;
    color: #6b7280;
  }

  .value {
    flex: 1;
    color: #1f2937;
    font-weight: 500;
  }
}

.verify-notice {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fef3c7;
  border-radius: 8px;
  margin-bottom: 32px;
}

.notice-icon {
  font-size: 20px;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
  color: #92400e;
  margin-bottom: 4px;
}

.notice-desc {
  font-size: 13px;
  color: #92400e;
  line-height: 1.6;
}

/* 认证提示 */
.auth-tips {
  margin-top: 32px;
  padding: 20px;
  background: #eff6ff;
  border-radius: 8px;

  h4 {
    font-size: 14px;
    font-weight: 600;
    color: #1e40af;
    margin-bottom: 12px;
  }

  ul {
    list-style: none;
    padding: 0;
  }

  li {
    font-size: 13px;
    color: #4b5563;
    padding: 4px 0;
    padding-left: 20px;
    position: relative;

    &::before {
      content: '•';
      position: absolute;
      left: 8px;
      color: #1e40af;
    }
  }
}

/* 认证成功 */
.auth-success {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 80px;
  margin-bottom: 24px;
}

.success-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.success-desc {
  font-size: 16px;
  color: #6b7280;
  margin-bottom: 32px;
}

.success-info {
  background: #f9fafb;
  border-radius: 12px;
  padding: 24px;
  text-align: left;
  margin-bottom: 32px;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.success-info-item {
  display: flex;
  padding: 8px 0;
  font-size: 14px;

  .label {
    width: 80px;
    color: #6b7280;
  }

  .value {
    flex: 1;
    color: #1f2937;
    font-weight: 500;
  }
}

.success-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .auth-header {
    padding: 32px 24px;
  }

  .auth-body {
    padding: 24px;
  }

  .steps {
    flex-direction: column;
    gap: 16px;
  }

  .step-item:not(:last-child)::after {
    display: none;
  }
}
</style>
