<template>
  <div class="page-container">
    <div class="profile-layout">
      <!-- 左侧用户卡片 -->
      <div class="user-sidebar">
        <div class="user-card">
          <div class="user-card-header">
            <div class="user-avatar">
              <el-avatar :size="80" :src="userInfo.avatar">
                {{ userInfo.nickname?.charAt(0) || '👤' }}
              </el-avatar>
            </div>
            <div class="user-info">
              <div class="user-name">{{ userInfo.nickname || '未设置昵称' }}</div>
              <div class="user-phone">{{ userInfo.phone }}</div>
              <div class="user-verify-badge" v-if="userInfo.realNameStatus === 1">
                ✅ 已实名认证
              </div>
            </div>
          </div>
          <div class="user-card-body">
            <div class="user-stats">
              <div class="user-stat" @click="router.push('/progress')">
                <div class="user-stat-number">0</div>
                <div class="user-stat-label">我的办件</div>
              </div>
              <div class="user-stat" @click="router.push('/my-license')">
                <div class="user-stat-number">0</div>
                <div class="user-stat-label">我的证照</div>
              </div>
              <div class="user-stat" @click="router.push('/message')">
                <div class="user-stat-number">0</div>
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
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="profile-content">
        <!-- 个人信息 -->
        <div class="content-card" v-show="activeSection === 'info'">
          <h3>
            <span class="icon">👤</span>
            个人信息
          </h3>

          <div class="verify-status" v-if="userInfo.realNameStatus === 1">
            <div class="verify-icon success">✅</div>
            <div class="verify-info">
              <div class="verify-title">已实名认证</div>
              <div class="verify-desc">认证姓名：{{ userInfo.realName }}</div>
            </div>
          </div>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="profileForm.nickname"
                placeholder="请输入昵称"
                maxlength="30"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="手机号">
              <el-input :value="userInfo.phone" disabled />
              <div class="form-hint">手机号不可修改</div>
            </el-form-item>

            <el-form-item label="性别" prop="sex">
              <el-select v-model="profileForm.sex" placeholder="请选择性别">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
                <el-option label="未知" :value="0" />
              </el-select>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="profileForm.email"
                placeholder="请输入邮箱"
                maxlength="50"
              />
            </el-form-item>

            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
              >
                <el-avatar :size="100" :src="profileForm.avatar">
                  {{ profileForm.nickname?.charAt(0) || '👤' }}
                </el-avatar>
                <div class="upload-tip">点击更换头像</div>
              </el-upload>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveProfile">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 修改密码 -->
        <div class="content-card" v-show="activeSection === 'password'">
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
                <div :class="['strength-bar', { active: passwordStrength >= 1 }, strengthLevel]"></div>
                <div :class="['strength-bar', { active: passwordStrength >= 2 }, strengthLevel]"></div>
                <div :class="['strength-bar', { active: passwordStrength >= 3 }, strengthLevel]"></div>
              </div>
              <div :class="['strength-text', strengthLevel]" v-if="passwordForm.newPassword">
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
        <div class="content-card" v-show="activeSection === 'security'">
          <h3>
            <span class="icon">🛡️</span>
            账号安全
          </h3>

          <div class="security-list">
            <div class="security-item">
              <div class="security-icon">📱</div>
              <div class="security-info">
                <div class="security-title">手机绑定</div>
                <div class="security-desc">已绑定：{{ userInfo.phone }}</div>
              </div>
              <span class="security-status status-active">已绑定</span>
            </div>

            <div class="security-item">
              <div class="security-icon">📧</div>
              <div class="security-info">
                <div class="security-title">邮箱绑定</div>
                <div class="security-desc">
                  {{ userInfo.email ? `已绑定：${maskEmail(userInfo.email)}` : '未绑定邮箱' }}
                </div>
              </div>
              <span :class="['security-status', userInfo.email ? 'status-active' : 'status-inactive']">
                {{ userInfo.email ? '已绑定' : '未绑定' }}
              </span>
            </div>

            <div class="security-item">
              <div class="security-icon">🔐</div>
              <div class="security-info">
                <div class="security-title">登录保护</div>
                <div class="security-desc">开启后登录需要验证码验证</div>
              </div>
              <span class="security-status status-active">已开启</span>
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

// 用户信息
const userInfo = ref({
  userId: '',
  phone: '',
  nickname: '',
  avatar: '',
  sex: 0,
  email: '',
  realNameStatus: 0,
  realName: ''
})

// 个人信息表单
const profileFormRef = ref()
const profileForm = reactive({
  nickname: '',
  sex: 0,
  email: '',
  avatar: ''
})

const profileRules = {
  nickname: [
    { max: 30, message: '昵称长度不能超过30个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
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
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度为8-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
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
    profileForm.nickname = data.nickname || ''
    profileForm.sex = data.sex || 0
    profileForm.email = data.email || ''
    profileForm.avatar = data.avatar || ''
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
      nickname: profileForm.nickname,
      sex: profileForm.sex,
      email: profileForm.email,
      avatar: profileForm.avatar
    })

    ElMessage.success('保存成功')
    // 刷新用户信息
    await fetchUserInfo()
    // 更新 store 中的用户信息
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
    // 清空表单
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

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

// 头像上传
const handleAvatarUpload = async (options) => {
  // 这里需要调用上传接口，暂时使用本地预览
  const file = options.file
  const reader = new FileReader()
  reader.onload = (e) => {
    profileForm.avatar = e.target.result
  }
  reader.readAsDataURL(file)
}

// 邮箱脱敏
const maskEmail = (email) => {
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
  background: linear-gradient(135deg, #1E40AF 0%, #3B82F6 100%);
  padding: 32px 24px;
  text-align: center;
  color: white;
}

.user-avatar {
  margin: 0 auto 16px;
}

.user-info {
  text-align: center;
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
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 20px;
}

.user-stat {
  text-align: center;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: var(--primary);
  }
}

.user-stat-number {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
}

.user-stat-label {
  font-size: 12px;
  color: var(--text-secondary);
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
  color: var(--text-regular);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--bg-hover);
    color: var(--primary);
  }

  &.active {
    background: var(--primary-bg);
    color: var(--primary);
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
  color: var(--text-secondary);
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
    color: var(--text-primary);
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
  background: var(--bg-hover);
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
    background: var(--success-bg);
  }
}

.verify-info {
  flex: 1;
}

.verify-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.verify-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 表单样式 */
.profile-form {
  max-width: 600px;
}

.password-form {
  max-width: 400px;
}

.form-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 6px;
}

/* 头像上传 */
.avatar-uploader {
  text-align: center;
  cursor: pointer;

  .upload-tip {
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 8px;
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
  background: var(--bg-hover);
  border-radius: 12px;
  gap: 16px;
}

.security-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--primary-bg);
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
  color: var(--text-primary);
  margin-bottom: 4px;
}

.security-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

.security-status {
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 6px;

  &.status-active {
    background: var(--success-bg);
    color: var(--success);
  }

  &.status-inactive {
    background: var(--bg-hover);
    color: var(--text-secondary);
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

  .security-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
