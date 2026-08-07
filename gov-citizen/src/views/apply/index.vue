<template>
  <div class="apply-page">
    <div class="page-container">
      <div class="container">
        <!-- 面包屑导航 -->
        <div class="breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="separator">/</span>
          <router-link to="/items">事项办理</router-link>
          <span class="separator">/</span>
          <router-link :to="`/items/${route.params.id}`">事项详情</router-link>
          <span class="separator">/</span>
          <span class="current">在线申请</span>
        </div>

        <!-- 步骤指示器 -->
        <div class="steps-bar">
          <div class="steps">
            <div
              v-for="(step, index) in steps"
              :key="index"
              :class="[
                'step-item',
                { active: currentStep === index, completed: currentStep > index }
              ]"
            >
              <div class="step-circle">{{ currentStep > index ? '✓' : index + 1 }}</div>
              <div class="step-label">{{ step }}</div>
            </div>
          </div>
        </div>

        <!-- 申请表单 -->
        <template v-if="!submitSuccess">
          <div class="apply-layout">
            <!-- 左侧表单 -->
            <div class="apply-main">
              <!-- 步骤1：基本信息 -->
              <template v-if="currentStep === 0">
                <div class="form-card">
                  <h3>
                    <span class="icon">👤</span>
                    基本信息
                  </h3>
                  <div class="form-row">
                    <div class="form-group">
                      <label class="form-label">姓名 <span class="required">*</span></label>
                      <el-input v-model="formData.name" placeholder="请输入姓名" />
                    </div>
                    <div class="form-group">
                      <label class="form-label">身份证号 <span class="required">*</span></label>
                      <el-input
                        v-model="formData.idCard"
                        placeholder="请输入身份证号"
                        maxlength="18"
                      />
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-group">
                      <label class="form-label">手机号 <span class="required">*</span></label>
                      <el-input
                        v-model="formData.phone"
                        placeholder="请输入手机号"
                        maxlength="11"
                      />
                    </div>
                    <div class="form-group">
                      <label class="form-label">联系地址 <span class="required">*</span></label>
                      <el-input v-model="formData.address" placeholder="请输入联系地址" />
                    </div>
                  </div>
                </div>

                <div class="form-card">
                  <h3>
                    <span class="icon">📋</span>
                    业务信息
                  </h3>
                  <div class="form-row">
                    <div class="form-group">
                      <label class="form-label">申请类型 <span class="required">*</span></label>
                      <el-select
                        v-model="formData.applyType"
                        placeholder="请选择申请类型"
                        style="width: 100%"
                      >
                        <el-option label="首次申领" value="first" />
                        <el-option label="换领" value="renew" />
                        <el-option label="补领" value="replace" />
                      </el-select>
                    </div>
                    <div class="form-group">
                      <label class="form-label">领取方式 <span class="required">*</span></label>
                      <el-select
                        v-model="formData.receiveType"
                        placeholder="请选择领取方式"
                        style="width: 100%"
                      >
                        <el-option label="本人领取" value="self" />
                        <el-option label="邮寄送达" value="mail" />
                        <el-option label="代领" value="agent" />
                      </el-select>
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-group">
                      <label class="form-label">领取地点 <span class="required">*</span></label>
                      <el-select
                        v-model="formData.receiveLocation"
                        placeholder="请选择领取地点"
                        style="width: 100%"
                      >
                        <el-option label="海口市公安局户政大厅" value="1" />
                        <el-option label="龙华区政务服务中心" value="2" />
                        <el-option label="美兰区政务服务中心" value="3" />
                      </el-select>
                    </div>
                    <div class="form-group">
                      <label class="form-label">有效期 <span class="required">*</span></label>
                      <el-select
                        v-model="formData.validPeriod"
                        placeholder="请选择有效期"
                        style="width: 100%"
                      >
                        <el-option label="10年" value="10" />
                        <el-option label="20年" value="20" />
                        <el-option label="长期" value="long" />
                      </el-select>
                    </div>
                  </div>
                  <div class="form-group">
                    <label class="form-label">备注说明</label>
                    <el-input
                      v-model="formData.remark"
                      type="textarea"
                      :rows="3"
                      placeholder="如有特殊情况请在此说明（选填）"
                    />
                  </div>
                </div>
              </template>

              <!-- 步骤2：上传材料 -->
              <template v-if="currentStep === 1">
                <div class="form-card">
                  <h3>
                    <span class="icon">📎</span>
                    材料上传
                  </h3>

                  <div class="material-checklist">
                    <div
                      v-for="(material, index) in materials"
                      :key="index"
                      :class="[
                        'checklist-item',
                        material.required ? 'required' : 'optional',
                        { uploaded: material.uploaded }
                      ]"
                    >
                      <el-checkbox v-model="material.checked" :disabled="!material.uploaded" />
                      <div class="checklist-info">
                        <div class="checklist-name">{{ material.name }}</div>
                        <div class="checklist-desc">{{ material.desc }}</div>
                      </div>
                      <span
                        :class="[
                          'checklist-status',
                          material.uploaded
                            ? 'uploaded'
                            : material.required
                              ? 'required'
                              : 'optional'
                        ]"
                      >
                        {{ material.uploaded ? '已上传' : '待上传' }}
                      </span>
                    </div>
                  </div>

                  <el-upload
                    class="upload-area"
                    drag
                    action="#"
                    :auto-upload="false"
                    :on-change="handleFileChange"
                    accept=".jpg,.jpeg,.png,.pdf"
                  >
                    <div class="upload-icon">📤</div>
                    <div class="upload-text">点击或拖拽文件到此区域上传</div>
                    <div class="upload-hint">支持 jpg、png、pdf 格式，单个文件不超过 10MB</div>
                  </el-upload>

                  <div v-if="uploadedFiles.length > 0" class="uploaded-list">
                    <div v-for="(file, index) in uploadedFiles" :key="index" class="uploaded-item">
                      <span class="uploaded-icon">{{ file.type === 'pdf' ? '📄' : '🖼️' }}</span>
                      <div class="uploaded-info">
                        <div class="uploaded-name">{{ file.name }}</div>
                        <div class="uploaded-size">{{ file.size }}</div>
                      </div>
                      <div class="uploaded-actions">
                        <el-button text type="primary" size="small" @click="handlePreview(file)"
                          >预览</el-button
                        >
                        <el-button text type="danger" size="small" @click="handleDeleteFile(index)"
                          >删除</el-button
                        >
                      </div>
                    </div>
                  </div>
                </div>
              </template>

              <!-- 步骤3：确认提交 -->
              <template v-if="currentStep === 2">
                <div class="form-card">
                  <h3>
                    <span class="icon">✅</span>
                    确认信息
                  </h3>

                  <div class="confirm-section">
                    <h4>基本信息</h4>
                    <div class="confirm-grid">
                      <div class="confirm-item">
                        <span class="confirm-label">姓名</span>
                        <span class="confirm-value">{{ formData.name }}</span>
                      </div>
                      <div class="confirm-item">
                        <span class="confirm-label">身份证号</span>
                        <span class="confirm-value">{{ formData.idCard }}</span>
                      </div>
                      <div class="confirm-item">
                        <span class="confirm-label">手机号</span>
                        <span class="confirm-value">{{ formData.phone }}</span>
                      </div>
                      <div class="confirm-item">
                        <span class="confirm-label">联系地址</span>
                        <span class="confirm-value">{{ formData.address }}</span>
                      </div>
                    </div>
                  </div>

                  <el-divider />

                  <div class="confirm-section">
                    <h4>业务信息</h4>
                    <div class="confirm-grid">
                      <div class="confirm-item">
                        <span class="confirm-label">申请类型</span>
                        <span class="confirm-value">{{
                          getApplyTypeText(formData.applyType)
                        }}</span>
                      </div>
                      <div class="confirm-item">
                        <span class="confirm-label">领取方式</span>
                        <span class="confirm-value">{{
                          getReceiveTypeText(formData.receiveType)
                        }}</span>
                      </div>
                      <div class="confirm-item">
                        <span class="confirm-label">领取地点</span>
                        <span class="confirm-value">{{
                          getLocationText(formData.receiveLocation)
                        }}</span>
                      </div>
                      <div class="confirm-item">
                        <span class="confirm-label">有效期</span>
                        <span class="confirm-value">{{
                          getValidPeriodText(formData.validPeriod)
                        }}</span>
                      </div>
                    </div>
                  </div>

                  <el-divider />

                  <div class="confirm-section">
                    <h4>上传材料</h4>
                    <div class="confirm-files">
                      <div v-for="(file, index) in uploadedFiles" :key="index" class="confirm-file">
                        <span>{{ file.type === 'pdf' ? '📄' : '🖼️' }}</span>
                        <span>{{ file.name }}</span>
                      </div>
                      <div v-if="uploadedFiles.length === 0" class="confirm-empty">
                        暂无上传材料
                      </div>
                    </div>
                  </div>

                  <el-checkbox v-model="agreed" class="agree-checkbox">
                    我已确认以上信息无误，并同意
                    <a href="#" @click.prevent="showAgreement">《在线办理服务协议》</a>
                  </el-checkbox>
                </div>
              </template>

              <!-- 底部操作栏 -->
              <div class="action-bar">
                <div class="action-left">
                  <el-button @click="handleSaveDraft">暂存草稿</el-button>
                </div>
                <div class="action-right">
                  <el-button v-if="currentStep > 0" @click="handlePrev">上一步</el-button>
                  <el-button v-if="currentStep < 2" type="primary" @click="handleNext"
                    >下一步</el-button
                  >
                  <el-button
                    v-if="currentStep === 2"
                    type="primary"
                    :disabled="!agreed"
                    :loading="submitting"
                    @click="handleSubmit"
                  >
                    提交申请
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 右侧边栏 -->
            <div class="apply-sidebar">
              <!-- 申请人信息 -->
              <div class="sidebar-card">
                <h3>👤 申请人信息</h3>
                <div class="info-list">
                  <div class="info-row">
                    <span class="info-label">姓名</span>
                    <span class="info-value">{{ formData.name || '-' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">身份证</span>
                    <span class="info-value">{{ maskIdCard(formData.idCard) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">手机号</span>
                    <span class="info-value">{{ maskPhone(formData.phone) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">认证状态</span>
                    <span class="info-value verified">已实名认证</span>
                  </div>
                </div>
              </div>

              <!-- 申请事项 -->
              <div class="sidebar-card">
                <h3>📋 申请事项</h3>
                <div class="info-list">
                  <div class="info-row">
                    <span class="info-label">事项</span>
                    <span class="info-value">{{ itemInfo.name }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">部门</span>
                    <span class="info-value">{{ itemInfo.dept }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">时限</span>
                    <span class="info-value highlight">{{ itemInfo.duration }}</span>
                  </div>
                </div>
              </div>

              <!-- 办理提示 -->
              <div class="sidebar-card">
                <h3>💡 办理提示</h3>
                <div class="tips-list">
                  <div class="tip-item">
                    <span class="tip-icon">1</span>
                    <span class="tip-text">请确保填写的信息真实有效</span>
                  </div>
                  <div class="tip-item">
                    <span class="tip-icon">2</span>
                    <span class="tip-text">上传材料需清晰完整</span>
                  </div>
                  <div class="tip-item">
                    <span class="tip-icon">3</span>
                    <span class="tip-text">提交后可在"我的办件"查看进度</span>
                  </div>
                  <div class="tip-item">
                    <span class="tip-icon">4</span>
                    <span class="tip-text">如有疑问请拨打 0898-12345</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 成功页面 -->
        <template v-else>
          <div class="success-page">
            <div class="success-icon">✅</div>
            <h2 class="success-title">申请提交成功！</h2>
            <p class="success-desc">您的申请已成功提交，请耐心等待审核</p>
            <div class="success-info">
              <div class="success-item">
                <span class="success-label">办件编号</span>
                <span class="success-value">{{ submitResult.orderNo }}</span>
              </div>
              <div class="success-item">
                <span class="success-label">申请事项</span>
                <span class="success-value">{{ submitResult.itemName }}</span>
              </div>
              <div class="success-item">
                <span class="success-label">提交时间</span>
                <span class="success-value">{{ submitResult.submitTime }}</span>
              </div>
              <div class="success-item">
                <span class="success-label">预计完成</span>
                <span class="success-value">{{ submitResult.estimateTime }}</span>
              </div>
            </div>
            <div class="success-actions">
              <el-button type="primary" size="large" @click="router.push('/profile')"
                >查看办件进度</el-button
              >
              <el-button size="large" @click="router.push('/')">返回首页</el-button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

// ========== 步骤 ==========
const steps = ['填写信息', '上传材料', '确认提交']
const currentStep = ref(0)

// ========== 表单数据 ==========
const formData = ref({
  name: '',
  idCard: '',
  phone: '',
  address: '',
  applyType: '',
  receiveType: 'self',
  receiveLocation: '',
  validPeriod: '10',
  remark: ''
})

// ========== 事项信息 ==========
const itemInfo = ref({
  name: '居民身份证办理',
  dept: '公安局',
  duration: '5个工作日'
})

// 根据路由参数获取事项信息
onMounted(() => {
  // TODO: 根据 route.params.id 从接口获取事项信息
  // 模拟数据
  const itemId = route.params.id
  if (itemId) {
    // 实际应调用 API 获取事项详情
    itemInfo.value = {
      name: '居民身份证办理',
      dept: '公安局',
      duration: '5个工作日'
    }
  }
})

// ========== 材料清单 ==========
const materials = ref([
  {
    name: '居民户口簿',
    desc: '请上传户口簿首页和本人页照片',
    required: true,
    uploaded: false,
    checked: false
  },
  {
    name: '本人近期免冠照片',
    desc: '白底彩色照片，可现场拍摄',
    required: true,
    uploaded: false,
    checked: false
  },
  {
    name: '原居民身份证',
    desc: '换领时需要提供原件照片',
    required: false,
    uploaded: false,
    checked: false
  }
])

// ========== 已上传文件 ==========
const uploadedFiles = ref([])

const handleFileChange = file => {
  // 验证文件大小
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }

  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'application/pdf']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.error('只支持 jpg、png、pdf 格式文件')
    return false
  }

  uploadedFiles.value.push({
    name: file.name,
    size: (file.size / 1024 / 1024).toFixed(2) + ' MB',
    type: file.name.split('.').pop(),
    raw: file.raw
  })

  // 根据文件名判断材料类型并更新状态
  updateMaterialStatus()

  ElMessage.success('文件上传成功')
}

const updateMaterialStatus = () => {
  // 简单模拟：每上传一个文件，自动标记一个未上传的必传材料
  const unuploadedRequired = materials.value.find(m => m.required && !m.uploaded)
  if (unuploadedRequired) {
    unuploadedRequired.uploaded = true
    unuploadedRequired.checked = true
  }
}

const handlePreview = file => {
  ElMessage.info(`预览文件: ${file.name}`)
}

const handleDeleteFile = index => {
  const deletedFile = uploadedFiles.value[index]
  uploadedFiles.value.splice(index, 1)

  // 重置对应的材料状态
  const matchedMaterial = materials.value.find(m => m.uploaded && m.checked)
  if (matchedMaterial) {
    matchedMaterial.uploaded = false
    matchedMaterial.checked = false
  }

  ElMessage.success('文件已删除')
}

// ========== 确认信息 ==========
const agreed = ref(false)

const getApplyTypeText = type => {
  const map = { first: '首次申领', renew: '换领', replace: '补领' }
  return map[type] || '-'
}

const getReceiveTypeText = type => {
  const map = { self: '本人领取', mail: '邮寄送达', agent: '代领' }
  return map[type] || '-'
}

const getLocationText = location => {
  const map = { 1: '海口市公安局户政大厅', 2: '龙华区政务服务中心', 3: '美兰区政务服务中心' }
  return map[location] || '-'
}

const getValidPeriodText = period => {
  const map = { 10: '10年', 20: '20年', long: '长期' }
  return map[period] || '-'
}

const showAgreement = () => {
  ElMessageBox.alert(
    '本协议是用户与海口政务服务平台之间关于在线办理政务服务事项所订立的契约...',
    '在线办理服务协议',
    { confirmButtonText: '我已阅读' }
  )
}

// ========== 数据脱敏 ==========
const maskIdCard = idCard => {
  if (!idCard) return '-'
  return idCard.replace(/^(.{6})(.*)(.{4})$/, '$1****$3')
}

const maskPhone = phone => {
  if (!phone) return '-'
  return phone.replace(/^(.{3})(.*)(.{4})$/, '$1****$3')
}

// ========== 步骤操作 ==========
const handlePrev = () => {
  currentStep.value--
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleNext = () => {
  // 验证当前步骤
  if (currentStep.value === 0) {
    if (!validateStep1()) return
  }

  if (currentStep.value === 1) {
    if (!validateStep2()) return
  }

  currentStep.value++
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 步骤1验证
const validateStep1 = () => {
  const { name, idCard, phone, address, applyType, receiveType, receiveLocation, validPeriod } =
    formData.value

  if (!name || !idCard || !phone || !address) {
    ElMessage.warning('请填写所有必填的基本信息')
    return false
  }

  // 手机号格式验证
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('请输入正确的手机号')
    return false
  }

  // 身份证格式验证
  if (!/^\d{17}[\dXx]$/.test(idCard)) {
    ElMessage.warning('请输入正确的身份证号')
    return false
  }

  if (!applyType || !receiveType || !receiveLocation || !validPeriod) {
    ElMessage.warning('请填写所有必填的业务信息')
    return false
  }

  return true
}

// 步骤2验证
const validateStep2 = () => {
  const requiredMaterials = materials.value.filter(m => m.required)
  const unuploadedRequired = requiredMaterials.filter(m => !m.uploaded)

  if (unuploadedRequired.length > 0) {
    ElMessage.warning(`请上传必传材料：${unuploadedRequired.map(m => m.name).join('、')}`)
    return false
  }

  return true
}

const handleSaveDraft = () => {
  // 模拟保存草稿到本地存储
  const draftData = {
    formData: formData.value,
    currentStep: currentStep.value,
    savedAt: new Date().toISOString()
  }
  localStorage.setItem('applyDraft', JSON.stringify(draftData))
  ElMessage.success('草稿已保存')
}

// ========== 提交 ==========
const submitSuccess = ref(false)
const submitting = ref(false)
const submitResult = ref({
  orderNo: '',
  itemName: '',
  submitTime: '',
  estimateTime: ''
})

const handleSubmit = async () => {
  if (!agreed.value) {
    ElMessage.warning('请阅读并同意服务协议')
    return
  }

  try {
    await ElMessageBox.confirm('提交后将进入审核流程，请确认信息填写无误', '确认提交申请', {
      confirmButtonText: '确认提交',
      cancelButtonText: '返回修改',
      type: 'warning'
    })

    submitting.value = true

    // 模拟提交请求
    await new Promise(resolve => setTimeout(resolve, 1500))

    // 生成办件编号
    const now = new Date()
    const orderNo =
      'BJ' +
      now.getFullYear() +
      String(now.getMonth() + 1).padStart(2, '0') +
      String(now.getDate()).padStart(2, '0') +
      String(Math.floor(Math.random() * 10000)).padStart(4, '0')

    submitResult.value = {
      orderNo,
      itemName: itemInfo.value.name,
      submitTime: now.toLocaleString('zh-CN'),
      estimateTime: new Date(now.getTime() + 5 * 24 * 60 * 60 * 1000).toLocaleDateString('zh-CN')
    }

    // 清除草稿
    localStorage.removeItem('applyDraft')

    ElMessage.success('提交成功')
    submitSuccess.value = true
  } catch {
    // 取消
  } finally {
    submitting.value = false
  }
}

// ========== 初始化 ==========
onMounted(() => {
  // 检查是否有草稿
  const draft = localStorage.getItem('applyDraft')
  if (draft) {
    try {
      const draftData = JSON.parse(draft)
      ElMessageBox.confirm('检测到未完成的申请，是否恢复？', '恢复草稿', {
        confirmButtonText: '恢复',
        cancelButtonText: '重新填写',
        type: 'info'
      })
        .then(() => {
          formData.value = draftData.formData
          currentStep.value = draftData.currentStep
          ElMessage.success('草稿已恢复')
        })
        .catch(() => {
          localStorage.removeItem('applyDraft')
        })
    } catch (e) {
      localStorage.removeItem('applyDraft')
    }
  }
})
</script>

<style lang="scss" scoped>
.apply-page {
  min-height: 100vh;
  background: #f3f4f6;
}

.page-container {
  padding: 24px 0 48px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 面包屑导航 */
.breadcrumb {
  padding: 16px 0;
  font-size: 14px;
  color: #6b7280;

  a {
    color: #3b82f6;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  .separator {
    margin: 0 8px;
  }

  .current {
    color: #1f2937;
  }
}

/* 步骤指示器 */
.steps-bar {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.steps {
  display: flex;
  justify-content: center;
  gap: 0;
  max-width: 600px;
  margin: 0 auto;
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
    z-index: 0;
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
  transition: all 0.3s;
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
  font-size: 14px;
  color: #6b7280;
}

.step-item.active .step-label {
  color: #1e40af;
  font-weight: 600;
}

.step-item.completed .step-label {
  color: #10b981;
}

/* 主体布局 */
.apply-layout {
  display: flex;
  gap: 24px;
}

.apply-main {
  flex: 1;
  min-width: 0;
}

.apply-sidebar {
  width: 300px;
  flex-shrink: 0;
}

/* 表单卡片 */
.form-card {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    gap: 10px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f3f4f6;

    .icon {
      font-size: 22px;
    }
  }
}

.form-row {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.form-group {
  flex: 1;
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;

  .required {
    color: #ef4444;
    margin-left: 4px;
  }
}

/* 材料清单 */
.material-checklist {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.checklist-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border-left: 3px solid #e5e7eb;

  &.required {
    border-left-color: #ef4444;
  }

  &.optional {
    border-left-color: #6b7280;
  }

  &.uploaded {
    border-left-color: #10b981;
    background: #d1fae5;
  }
}

.checklist-info {
  flex: 1;
}

.checklist-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.checklist-desc {
  font-size: 12px;
  color: #6b7280;
}

.checklist-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;

  &.required {
    background: #fee2e2;
    color: #ef4444;
  }

  &.optional {
    background: #f3f4f6;
    color: #6b7280;
  }

  &.uploaded {
    background: #d1fae5;
    color: #10b981;
  }
}

/* 上传区域 */
.upload-area {
  :deep(.el-upload-dragger) {
    border: 2px dashed #e5e7eb;
    border-radius: 8px;
    padding: 32px;
    text-align: center;
    background: white;

    &:hover {
      border-color: #93c5fd;
      background: #eff6ff;
    }
  }
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.upload-text {
  font-size: 14px;
  color: #374151;
  margin-bottom: 4px;
}

.upload-hint {
  font-size: 12px;
  color: #6b7280;
}

/* 已上传文件 */
.uploaded-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.uploaded-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.uploaded-icon {
  font-size: 24px;
}

.uploaded-info {
  flex: 1;
}

.uploaded-name {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 2px;
}

.uploaded-size {
  font-size: 12px;
  color: #6b7280;
}

.uploaded-actions {
  display: flex;
  gap: 8px;
}

/* 确认信息 */
.confirm-section {
  margin-bottom: 24px;

  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
  }
}

.confirm-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.confirm-item {
  display: flex;
  gap: 12px;
}

.confirm-label {
  width: 80px;
  color: #6b7280;
  flex-shrink: 0;
}

.confirm-value {
  color: #1f2937;
}

.confirm-files {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.confirm-file {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #374151;
}

.confirm-empty {
  font-size: 14px;
  color: #9ca3af;
}

.agree-checkbox {
  margin-top: 24px;

  a {
    color: #3b82f6;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

/* 底部操作栏 */
.action-bar {
  background: white;
  border-radius: 12px;
  padding: 20px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  bottom: 24px;
}

.action-left,
.action-right {
  display: flex;
  gap: 12px;
}

/* 右侧边栏 */
.sidebar-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  font-size: 14px;
}

.info-label {
  width: 70px;
  color: #6b7280;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #1f2937;

  &.verified {
    color: #10b981;
  }

  &.highlight {
    color: #1e40af;
    font-weight: 500;
  }
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tip-item {
  display: flex;
  gap: 12px;
  font-size: 13px;
}

.tip-icon {
  width: 24px;
  height: 24px;
  background: #eff6ff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
  color: #1e40af;
}

.tip-text {
  color: #374151;
  line-height: 1.6;
}

/* 成功页面 */
.success-page {
  text-align: center;
  padding: 80px 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
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
  border-radius: 8px;
  padding: 24px;
  text-align: left;
  margin-bottom: 32px;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.success-item {
  display: flex;
  padding: 8px 0;
  font-size: 14px;
}

.success-label {
  width: 80px;
  color: #6b7280;
}

.success-value {
  flex: 1;
  color: #1f2937;
  font-weight: 500;
}

.success-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 1024px) {
  .apply-layout {
    flex-direction: column;
  }

  .apply-sidebar {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .steps {
    flex-direction: column;
    gap: 16px;
  }

  .step-item:not(:last-child)::after {
    display: none;
  }

  .form-row {
    flex-direction: column;
  }

  .confirm-grid {
    grid-template-columns: 1fr;
  }

  .action-bar {
    flex-direction: column;
    gap: 16px;
  }

  .action-left,
  .action-right {
    width: 100%;

    .el-button {
      flex: 1;
    }
  }
}
</style>
