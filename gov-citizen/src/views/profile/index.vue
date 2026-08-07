<template>
  <div class="page-container">
    <div class="profile-layout">
      <!-- 左侧用户卡片 -->
      <div class="user-sidebar">
        <div class="user-card">
          <div class="user-card-header">
            <div class="user-avatar">👤</div>
            <div>
              <div class="user-name">{{ userInfo.nickname || '未设置昵称' }}</div>
              <div class="user-phone">{{ maskPhone(userInfo.phone) }}</div>
              <div v-if="userInfo.realNameStatus === 1" class="user-verify-badge">
                ✅ 已实名认证
              </div>
            </div>
          </div>
          <div class="user-card-body">
            <div class="user-stats">
              <div class="user-stat" @click="router.push('/progress')">
                <div class="user-stat-number">{{ stats.applyCount }}</div>
                <div class="user-stat-label">我的办件</div>
              </div>
              <div class="user-stat" @click="router.push('/my-license')">
                <div class="user-stat-number">{{ stats.licenseCount }}</div>
                <div class="user-stat-label">我的证照</div>
              </div>
              <div class="user-stat" @click="router.push('/message')">
                <div class="user-stat-number">{{ stats.messageCount }}</div>
                <div class="user-stat-label">消息通知</div>
              </div>
            </div>

            <div class="user-menu">
              <div
                :class="['menu-item', { active: activeSection === 'info' }]"
                @click="activeSection = 'info'"
              >
                <span class="menu-icon">👤</span>
                <span class="menu-text">个人信息</span>
                <span class="menu-arrow">›</span>
              </div>
              <div
                :class="['menu-item', { active: activeSection === 'password' }]"
                @click="activeSection = 'password'"
              >
                <span class="menu-icon">🔒</span>
                <span class="menu-text">修改密码</span>
                <span class="menu-arrow">›</span>
              </div>
              <div
                :class="['menu-item', { active: activeSection === 'security' }]"
                @click="activeSection = 'security'"
              >
                <span class="menu-icon">🛡️</span>
                <span class="menu-text">账号安全</span>
                <span class="menu-arrow">›</span>
              </div>
              <div class="menu-item" @click="router.push('/verify')">
                <span class="menu-icon">🆔</span>
                <span class="menu-text">实名认证</span>
                <span class="menu-arrow">›</span>
              </div>
              <div class="menu-item" @click="router.push('/message')">
                <span class="menu-icon">🔔</span>
                <span class="menu-text">消息设置</span>
                <span class="menu-arrow">›</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="profile-content">
        <!-- 个人信息 -->
        <div v-show="activeSection === 'info'" class="content-card">
          <h3>
            <span class="icon">👤</span>
            个人信息
          </h3>

          <div v-if="userInfo.realNameStatus === 1" class="verify-status">
            <div class="verify-icon success">✅</div>
            <div class="verify-info">
              <div class="verify-title">已实名认证</div>
              <div class="verify-desc">
                认证姓名：{{ userInfo.realName }} | 认证时间：{{ userInfo.verifyTime }}
              </div>
            </div>
          </div>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <div class="form-row">
              <el-form-item label="姓名" prop="realName">
                <el-input
                  v-model="profileForm.realName"
                  disabled
                  placeholder="实名认证后不可修改"
                />
                <div class="form-hint">实名认证后不可修改</div>
              </el-form-item>
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="profileForm.idCard" disabled placeholder="实名认证后不可修改" />
                <div class="form-hint">实名认证后不可修改</div>
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="性别" prop="sex">
                <el-select v-model="profileForm.sex" placeholder="请选择性别" style="width: 100%">
                  <el-option label="男" :value="1" />
                  <el-option label="女" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item label="出生日期" prop="birthday">
                <el-date-picker
                  v-model="profileForm.birthday"
                  type="date"
                  placeholder="请选择出生日期"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </div>

            <el-form-item label="联系地址" prop="address">
              <el-input v-model="profileForm.address" placeholder="请输入联系地址" />
            </el-form-item>

            <el-form-item label="紧急联系人">
              <div class="emergency-contact">
                <el-input
                  v-model="profileForm.emergencyName"
                  placeholder="联系人姓名"
                  class="emergency-input"
                />
                <el-input
                  v-model="profileForm.emergencyPhone"
                  placeholder="联系人电话"
                  class="emergency-input"
                />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveProfile">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 修改密码 -->
        <div v-show="activeSection === 'password'" class="content-card">
          <h3>
            <span class="icon">🔒</span>
            修改密码
          </h3>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="password-form"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（8-20位）"
                show-password
                @input="checkPasswordStrength"
              />
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
              <div v-if="passwordForm.newPassword" :class="['strength-text', strengthLevel]">
                密码强度：{{ strengthText }}
              </div>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">
                确认修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 账号安全 -->
        <div v-show="activeSection === 'security'" class="content-card">
          <h3>
            <span class="icon">🛡️</span>
            账号安全
          </h3>

          <div class="security-list">
            <div class="security-item">
              <div class="security-icon">📱</div>
              <div class="security-info">
                <div class="security-title">手机绑定</div>
                <div class="security-desc">已绑定：{{ maskPhone(userInfo.phone) }}</div>
              </div>
              <span class="security-status status-active">已绑定</span>
              <button class="security-action">更换手机</button>
            </div>

            <div class="security-item">
              <div class="security-icon">📧</div>
              <div class="security-info">
                <div class="security-title">邮箱绑定</div>
                <div class="security-desc">
                  {{ userInfo.email ? `已绑定：${maskEmail(userInfo.email)}` : '未绑定邮箱' }}
                </div>
              </div>
              <span
                :class="['security-status', userInfo.email ? 'status-active' : 'status-inactive']"
              >
                {{ userInfo.email ? '已绑定' : '未绑定' }}
              </span>
              <button class="security-action">
                {{ userInfo.email ? '更换邮箱' : '立即绑定' }}
              </button>
            </div>

            <div class="security-item">
              <div class="security-icon">💬</div>
              <div class="security-info">
                <div class="security-title">微信绑定</div>
                <div class="security-desc">绑定后可使用微信快速登录</div>
              </div>
              <span class="security-status status-inactive">未绑定</span>
              <button class="security-action">立即绑定</button>
            </div>

            <div class="security-item">
              <div class="security-icon">💳</div>
              <div class="security-info">
                <div class="security-title">支付宝绑定</div>
                <div class="security-desc">绑定后可使用支付宝快速登录</div>
              </div>
              <span class="security-status status-inactive">未绑定</span>
              <button class="security-action">立即绑定</button>
            </div>

            <div class="security-item">
              <div class="security-icon">🔐</div>
              <div class="security-info">
                <div class="security-title">登录保护</div>
                <div class="security-desc">开启后登录需要验证码验证</div>
              </div>
              <span class="security-status status-active">已开启</span>
              <button class="security-action">设置</button>
            </div>

            <div class="security-item">
              <div class="security-icon">📋</div>
              <div class="security-info">
                <div class="security-title">登录日志</div>
                <div class="security-desc">查看账号登录历史记录</div>
              </div>
              <button class="security-action">查看</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo, changePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeSection = ref('info')

// 统计数据
const stats = ref({
  applyCount: 6,
  licenseCount: 5,
  messageCount: 3
})

// 用户信息
const userInfo = ref({
  userId: '',
  phone: '',
  nickname: '',
  avatar: '',
  sex: 0,
  email: '',
  realNameStatus: 0,
  realName: '',
  verifyTime: '2024-01-10'
})

// 个人信息表单
const profileFormRef = ref()
const profileForm = reactive({
  realName: '张三',
  idCard: '460100199001011234',
  phone: '13800138000',
  email: 'zhangsan@example.com',
  sex: 1,
  birthday: '1990-01-01',
  address: '海南省海口市龙华区XX路XX号',
  emergencyName: '李四',
  emergencyPhone: '13900139000'
})

const profileRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}

// 密码表单
const passwordFormRef = ref()
const passwordForm = reactive({
  oldPassword: '',
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
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
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

const strengthText = computed(() => {
  if (passwordStrength.value <= 1) return '弱'
  if (passwordStrength.value <= 2) return '中'
  return '强'
})

// 加载状态
const saving = ref(false)
const changingPassword = ref(false)

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const { data } = await getUserInfo()
    userInfo.value = data
    // 填充表单
    profileForm.phone = data.phone || ''
    profileForm.email = data.email || ''
    profileForm.sex = data.sex || 0
    profileForm.realName = data.realName || ''
    profileForm.idCard = data.idCard || ''
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 保存个人信息
const handleSaveProfile = async () => {
  try {
    await profileFormRef.value.validate()
    saving.value = true

    await updateUserInfo({
      phone: profileForm.phone,
      email: profileForm.email,
      sex: profileForm.sex,
      birthday: profileForm.birthday,
      address: profileForm.address,
      emergencyName: profileForm.emergencyName,
      emergencyPhone: profileForm.emergencyPhone
    })

    ElMessage.success('保存成功')
    await fetchUserInfo()
    userStore.userInfo.nickname = profileForm.nickname
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

// 检查密码强度
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

// 修改密码
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })

    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordStrength.value = 0
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    changingPassword.value = false
  }
}

// 数据脱敏
const maskPhone = phone => {
  if (!phone) return '-'
  return phone.replace(/^(.{3})(.*)(.{4})$/, '$1****$3')
}

const maskEmail = email => {
  if (!email) return ''
  const [username, domain] = email.split('@')
  const maskedUsername = username.charAt(0) + '****' + username.charAt(username.length - 1)
  return `${maskedUsername}@${domain}`
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-layout {
  display: flex;
  gap: 24px;
}

/* 左侧用户卡片 */
.user-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.user-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-card-header {
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  padding: 32px 24px;
  text-align: center;
  color: white;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 36px;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.user-phone {
  font-size: 14px;
  opacity: 0.9;
}

.user-verify-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  font-size: 12px;
}

.user-card-body {
  padding: 20px;
}

.user-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f3f4f6;
  margin-bottom: 20px;
}

.user-stat {
  text-align: center;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #1e40af;
  }
}

.user-stat-number {
  font-size: 20px;
  font-weight: 700;
  color: #1e40af;
}

.user-stat-label {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.user-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f9fafb;
    color: #1e40af;
  }

  &.active {
    background: #eff6ff;
    color: #1e40af;
    font-weight: 500;
  }
}

.menu-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.menu-text {
  flex: 1;
}

.menu-arrow {
  color: #6b7280;
}

/* 右侧内容 */
.profile-content {
  flex: 1;
  min-width: 0;
}

.content-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    gap: 10px;

    .icon {
      font-size: 22px;
    }
  }
}

/* 实名认证状态 */
.verify-status {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 24px;
}

.verify-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;

  &.success {
    background: #d1fae5;
  }
}

.verify-info {
  flex: 1;
}

.verify-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.verify-desc {
  font-size: 13px;
  color: #6b7280;
}

/* 表单样式 */
.profile-form {
  max-width: 100%;
}

.form-row {
  display: flex;
  gap: 24px;

  .el-form-item {
    flex: 1;
  }
}

.form-hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 6px;
}

.emergency-contact {
  display: flex;
  gap: 16px;
  width: 100%;

  .emergency-input {
    flex: 1;
  }
}

/* 密码表单 */
.password-form {
  max-width: 400px;
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

.strength-text {
  font-size: 12px;
  margin-top: 4px;

  &.weak {
    color: #ef4444;
  }
  &.medium {
    color: #f59e0b;
  }
  &.strong {
    color: #10b981;
  }
}

/* 账号安全 */
.security-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
  gap: 16px;
}

.security-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.security-info {
  flex: 1;
}

.security-title {
  font-size: 15px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.security-desc {
  font-size: 13px;
  color: #6b7280;
}

.security-status {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 6px;

  &.status-active {
    background: #d1fae5;
    color: #059669;
  }

  &.status-inactive {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.security-action {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  background: white;
  border: 1px solid #e5e7eb;
  color: #4b5563;
  transition: all 0.2s;

  &:hover {
    border-color: #1e40af;
    color: #1e40af;
  }
}

/* 响应式 */
@media (max-width: 1024px) {
  .profile-layout {
    flex-direction: column;
  }

  .user-sidebar {
    width: 100%;
  }

  .user-card-header {
    display: flex;
    align-items: center;
    gap: 20px;
    text-align: left;
  }

  .user-avatar {
    margin: 0;
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .content-card {
    padding: 20px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .security-item {
    flex-wrap: wrap;
  }

  .security-action {
    width: 100%;
    text-align: center;
  }

  .emergency-contact {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
