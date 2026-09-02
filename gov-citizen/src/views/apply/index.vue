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

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-card">
          <div class="loading-spinner"></div>
          <p>加载表单模板中...</p>
        </div>

        <!-- 无模板提示 -->
        <div v-else-if="!template" class="empty-card">
          <div class="empty-icon">📋</div>
          <h3>暂无表单模板</h3>
          <p>该事项尚未配置申请表单，请联系管理员</p>
          <el-button type="primary" @click="router.push(`/items/${route.params.id}`)">返回事项详情</el-button>
        </div>

        <!-- 申请表单 -->
        <template v-else-if="!submitSuccess">
          <div class="apply-layout">
            <!-- 左侧表单 -->
            <div class="apply-main">
              <!-- 步骤1：动态表单字段 -->
              <template v-if="currentStep === 0">
                <div class="form-card">
                  <h3>
                    <span class="icon">📋</span>
                    {{ template.templateName || '申请表单' }}
                  </h3>

                  <!-- 动态渲染表单字段 -->
                  <div class="form-row" v-for="(row, rowIndex) in formRows" :key="rowIndex">
                    <div class="form-group" v-for="field in row" :key="field.name">
                      <label class="form-label">
                        {{ field.name }}
                        <span v-if="field.required" class="required">*</span>
                      </label>

                      <!-- input 输入框 -->
                      <el-input
                        v-if="field.type === 'input'"
                        v-model="formData[field.name]"
                        :placeholder="field.placeholder || `请输入${field.name}`"
                      />

                      <!-- number 数字输入 -->
                      <el-input-number
                        v-else-if="field.type === 'number'"
                        v-model="formData[field.name]"
                        :min="0"
                        :placeholder="field.placeholder || `请输入${field.name}`"
                        style="width: 100%"
                      />

                      <!-- textarea 文本域 -->
                      <el-input
                        v-else-if="field.type === 'textarea'"
                        v-model="formData[field.name]"
                        type="textarea"
                        :rows="3"
                        :placeholder="field.placeholder || `请输入${field.name}`"
                      />

                      <!-- radio 单选 -->
                      <el-radio-group v-else-if="field.type === 'radio'" v-model="formData[field.name]">
                        <el-radio v-for="opt in field.options" :key="opt" :label="opt">{{ opt }}</el-radio>
                      </el-radio-group>

                      <!-- select 下拉选择 -->
                      <el-select
                        v-else-if="field.type === 'select'"
                        v-model="formData[field.name]"
                        :placeholder="`请选择${field.name}`"
                        style="width: 100%"
                      >
                        <el-option v-for="opt in field.options" :key="opt" :label="opt" :value="opt" />
                      </el-select>

                      <!-- date 日期选择 -->
                      <el-date-picker
                        v-else-if="field.type === 'date'"
                        v-model="formData[field.name]"
                        type="date"
                        :placeholder="`请选择${field.name}`"
                        style="width: 100%"
                        value-format="YYYY-MM-DD"
                      />

                      <!-- daterange 日期范围 -->
                      <el-date-picker
                        v-else-if="field.type === 'daterange'"
                        v-model="formData[field.name]"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        style="width: 100%"
                        value-format="YYYY-MM-DD"
                      />

                      <!-- upload 文件上传 -->
                      <div v-else-if="field.type === 'upload'" class="field-upload">
                        <el-upload
                          action="#"
                          :auto-upload="false"
                          :accept="field.accept || 'image/*,.pdf'"
                          :limit="field.maxCount || 5"
                          :on-change="(file) => handleFieldFileChange(field.name, file)"
                          :file-list="uploadFiles[field.name] || []"
                        >
                          <el-button size="small" type="primary">选择文件</el-button>
                          <template #tip>
                            <div class="el-upload__tip">
                              支持 {{ field.accept || '图片/PDF' }} 格式，最多 {{ field.maxCount || 5 }} 个文件
                            </div>
                          </template>
                        </el-upload>
                      </div>
                    </div>
                  </div>

                  <!-- 备注 -->
                  <div class="form-group full-width">
                    <label class="form-label">备注说明</label>
                    <el-input
                      v-model="formData['备注说明']"
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

                  <!-- 材料清单 -->
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
                      <div class="checklist-info">
                        <div class="checklist-name">
                          {{ material.name }}
                          <span v-if="material.required" class="required-tag">必传</span>
                          <span v-else class="optional-tag">选传</span>
                          <el-button
                            v-if="material.exampleUrl"
                            type="primary"
                            link
                            size="small"
                            @click="handlePreviewExample(material)"
                          >
                            查看示例
                          </el-button>
                        </div>
                        <div class="checklist-desc">{{ material.desc }}</div>
                        <!-- 已上传文件名 -->
                        <div v-if="material.uploaded && material.fileName" class="checklist-file">
                          📎 {{ material.fileName }}
                          <el-button type="danger" link size="small" @click="handleRemoveMaterialFile(index)">
                            删除
                          </el-button>
                        </div>
                      </div>
                      <!-- 上传按钮 -->
                      <el-upload
                        action="#"
                        :auto-upload="false"
                        :show-file-list="false"
                        accept=".jpg,.jpeg,.png,.pdf"
                        :on-change="(file) => handleMaterialFileChange(index, file)"
                      >
                        <el-button :type="material.uploaded ? 'success' : 'primary'" size="small">
                          {{ material.uploaded ? '已上传' : '上传文件' }}
                        </el-button>
                      </el-upload>
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
                    <h4>申请信息</h4>
                    <div class="confirm-grid">
                      <div class="confirm-item" v-for="(value, key) in formData" :key="key">
                        <span class="confirm-label">{{ key }}</span>
                        <span class="confirm-value">{{ formatConfirmValue(key, value) }}</span>
                      </div>
                    </div>
                  </div>

                  <el-divider />

                  <div class="confirm-section">
                    <h4>上传材料</h4>
                    <div class="confirm-files">
                      <div v-for="(material, index) in materials" :key="index" class="confirm-file">
                        <span>{{ material.uploaded ? '✅' : '⬜' }}</span>
                        <span>{{ material.name }}</span>
                        <span v-if="material.fileName" class="confirm-file-name">({{ material.fileName }})</span>
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
                  <el-button v-if="currentStep < 2" type="primary" @click="handleNext">下一步</el-button>
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
                    <span class="info-value">{{ userInfo.realName || '-' }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">手机号</span>
                    <span class="info-value">{{ maskPhone(userInfo.phone) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">认证状态</span>
                    <span :class="['info-value', userInfo.realNameStatus === 1 ? 'verified' : '']">
                      {{ userInfo.realNameStatus === 1 ? '已实名认证' : '未认证' }}
                    </span>
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
              <el-button type="primary" size="large" @click="router.push('/progress')">查看办件进度</el-button>
              <el-button size="large" @click="router.push('/')">返回首页</el-button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 示例图片预览弹窗 -->
    <el-dialog
      v-model="previewVisible"
      :title="previewTitle"
      width="600px"
      append-to-body
    >
      <div class="preview-content">
        <img v-if="previewUrl" :src="previewUrl" alt="示例图片" style="max-width: 100%;" />
        <p v-else>暂无示例图片</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActiveFormTemplate, getItemDetail, getItemMaterials } from '@/api/item'
import { getUserInfo } from '@/api/user'

const router = useRouter()
const route = useRoute()

// ========== 步骤 ==========
const steps = ['填写信息', '上传材料', '确认提交']
const currentStep = ref(0)

// ========== 加载状态 ==========
const loading = ref(true)

// ========== 表单模板 ==========
const template = ref(null)
const formFields = ref([])

// ========== 表单数据 ==========
const formData = ref({})

// ========== 材料清单 ==========
const materials = ref([])

// ========== 上传文件 ==========
const uploadFiles = ref({})

// ========== 用户信息 ==========
const userInfo = ref({
  realName: '',
  phone: '',
  realNameStatus: 0
})

// ========== 事项信息 ==========
const itemInfo = ref({
  name: '',
  dept: '',
  duration: ''
})

// ========== 计算表单行（每行2个字段，过滤掉upload类型） ==========
const formRows = computed(() => {
  const rows = []
  // 只保留文字类字段，过滤掉 upload 和 hidden 类型
  const fields = formFields.value.filter(f => f.type !== 'hidden' && f.type !== 'upload')
  for (let i = 0; i < fields.length; i += 2) {
    rows.push(fields.slice(i, i + 2))
  }
  return rows
})

// ========== 加载数据 ==========
onMounted(async () => {
  const itemId = route.params.id
  if (!itemId) {
    ElMessage.error('缺少事项ID')
    router.push('/items')
    return
  }

  try {
    // 并行加载事项详情、表单模板、用户信息、材料列表
    const [itemRes, templateRes, userRes, materialsRes] = await Promise.all([
      getItemDetail(itemId).catch(() => null),
      getActiveFormTemplate(itemId).catch(() => null),
      getUserInfo().catch(() => null),
      getItemMaterials(itemId).catch(() => null)
    ])

    // 设置事项信息
    if (itemRes?.data) {
      itemInfo.value = {
        name: itemRes.data.itemName,
        dept: itemRes.data.deptName || '',
        duration: itemRes.data.processTime || '即时办结'
      }
    }

    // 设置用户信息
    if (userRes?.data) {
      userInfo.value = userRes.data
    }

    // 设置材料列表
    if (materialsRes?.data) {
      materials.value = materialsRes.data.map(m => ({
        materialId: m.materialId,
        name: m.materialName,
        desc: m.materialDesc || m.remark || '',
        required: m.required === 1,
        exampleUrl: m.exampleUrl || '',
        uploaded: false,
        checked: false,
        fileName: '',
        fileRaw: null
      }))
    }

    // 设置表单模板
    if (templateRes?.data) {
      template.value = templateRes.data
      // 解析表单配置
      try {
        const config = JSON.parse(templateRes.data.formConfig)
        formFields.value = config.fields || []
        // 初始化表单数据
        const initialData = {}
        formFields.value.forEach(field => {
          initialData[field.name] = field.default || ''
        })
        formData.value = initialData
      } catch (e) {
        console.error('解析表单模板失败:', e)
        ElMessage.error('表单模板配置错误')
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
})

// ========== 文件上传 ==========
const handleFieldFileChange = (fieldName, file) => {
  if (!uploadFiles.value[fieldName]) {
    uploadFiles.value[fieldName] = []
  }
  uploadFiles.value[fieldName].push(file)
}

// ========== 材料文件上传 ==========
const handleMaterialFileChange = (index, file) => {
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

  // 更新材料状态
  materials.value[index].uploaded = true
  materials.value[index].checked = true
  materials.value[index].fileName = file.name
  materials.value[index].fileRaw = file.raw

  ElMessage.success(`${materials.value[index].name} 上传成功`)
}

const handleRemoveMaterialFile = (index) => {
  materials.value[index].uploaded = false
  materials.value[index].checked = false
  materials.value[index].fileName = ''
  materials.value[index].fileRaw = null
  ElMessage.success('文件已删除')
}

// ========== 示例预览 ==========
const previewVisible = ref(false)
const previewTitle = ref('')
const previewUrl = ref('')

const handlePreviewExample = (material) => {
  previewTitle.value = `${material.name} - 示例`
  previewUrl.value = material.exampleUrl
  previewVisible.value = true
}

const handleDeleteFile = (index) => {
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

// ========== 格式化确认值 ==========
const formatConfirmValue = (key, value) => {
  if (!value && value !== 0) return '-'
  if (Array.isArray(value)) return value.join(', ')
  return String(value)
}

// ========== 数据脱敏 ==========
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
  // 步骤1：验证必填字段（只验证文字类字段，跳过upload类型）
  if (currentStep.value === 0) {
    const requiredFields = formFields.value.filter(f => f.required && f.type !== 'upload')
    for (const field of requiredFields) {
      const value = formData.value[field.name]
      if (!value && value !== 0) {
        ElMessage.warning(`请填写 ${field.name}`)
        return
      }
    }
  }

  // 步骤2：验证必传材料
  if (currentStep.value === 1) {
    const requiredMaterials = materials.value.filter(m => m.required)
    const unuploaded = requiredMaterials.filter(m => !m.uploaded)
    if (unuploaded.length > 0) {
      ElMessage.warning(`请上传必传材料：${unuploaded.map(m => m.name).join('、')}`)
      return
    }
  }

  currentStep.value++
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const showAgreement = () => {
  ElMessageBox.alert(
    '本协议是用户与海口政务服务平台之间关于在线办理政务服务事项所订立的契约...',
    '在线办理服务协议',
    { confirmButtonText: '我已阅读' }
  )
}

// ========== 暂存草稿 ==========
const handleSaveDraft = () => {
  const draftData = {
    itemId: route.params.id,
    formData: formData.value,
    currentStep: currentStep.value,
    savedAt: new Date().toISOString()
  }
  localStorage.setItem(`applyDraft_${route.params.id}`, JSON.stringify(draftData))
  ElMessage.success('草稿已保存')
}

// ========== 提交 ==========
const agreed = ref(false)
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

    // TODO: 调用后端提交接口
    await new Promise(resolve => setTimeout(resolve, 1500))

    // 生成办件编号
    const now = new Date()
    const orderNo = 'BJ' + now.getFullYear() +
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
    localStorage.removeItem(`applyDraft_${route.params.id}`)

    ElMessage.success('提交成功')
    submitSuccess.value = true
  } catch {
    // 取消
  } finally {
    submitting.value = false
  }
}

// ========== 初始化 - 检查草稿 ==========
onMounted(() => {
  const draft = localStorage.getItem(`applyDraft_${route.params.id}`)
  if (draft) {
    try {
      const draftData = JSON.parse(draft)
      ElMessageBox.confirm('检测到未完成的申请，是否恢复？', '恢复草稿', {
        confirmButtonText: '恢复',
        cancelButtonText: '重新填写',
        type: 'info'
      }).then(() => {
        formData.value = draftData.formData
        currentStep.value = draftData.currentStep
        ElMessage.success('草稿已恢复')
      }).catch(() => {
        localStorage.removeItem(`applyDraft_${route.params.id}`)
      })
    } catch (e) {
      localStorage.removeItem(`applyDraft_${route.params.id}`)
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

/* 加载和空状态 */
.loading-card,
.empty-card {
  background: white;
  border-radius: 12px;
  padding: 80px 40px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #e5e7eb;
    border-top-color: #1e40af;
    border-radius: 50%;
    margin: 0 auto 16px;
    animation: spin 1s linear infinite;
  }

  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
  }

  h3 {
    font-size: 18px;
    color: #1f2937;
    margin-bottom: 8px;
  }

  p {
    color: #6b7280;
    margin-bottom: 24px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
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
  max-width: 400px;
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

  &.full-width {
    width: 100%;
  }
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

.field-upload {
  :deep(.el-upload-list) {
    margin-top: 8px;
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
  display: flex;
  align-items: center;
  gap: 8px;
}

.required-tag {
  font-size: 12px;
  color: #ef4444;
  background: #fee2e2;
  padding: 1px 6px;
  border-radius: 4px;
}

.optional-tag {
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 1px 6px;
  border-radius: 4px;
}

/* 示例预览 */
.preview-content {
  text-align: center;
  padding: 20px;

  img {
    max-width: 100%;
    max-height: 500px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  p {
    color: #9ca3af;
    font-size: 14px;
  }
}

.checklist-desc {
  font-size: 12px;
  color: #6b7280;
}

.checklist-file {
  font-size: 12px;
  color: #10b981;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.confirm-file-name {
  font-size: 12px;
  color: #6b7280;
  margin-left: 4px;
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
.upload-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;

  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
  }
}

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
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;

  h4 {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
  }
}

.uploaded-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 8px;
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

/* 确认信息 - 文件列表 */
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
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.confirm-label {
  width: 100px;
  color: #6b7280;
  flex-shrink: 0;
  font-size: 14px;
}

.confirm-value {
  color: #1f2937;
  font-size: 14px;
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
