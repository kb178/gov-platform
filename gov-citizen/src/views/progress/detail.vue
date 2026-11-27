<template>
  <div class="detail-page">
    <div class="page-container">
      <div class="container">
        <!-- 面包屑 -->
        <div class="breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="separator">/</span>
          <router-link to="/progress">办件进度</router-link>
          <span class="separator">/</span>
          <span class="current">申请详情</span>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="loading-card">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- 详情内容 -->
        <template v-else-if="detail">
          <!-- 基本信息 -->
          <div class="detail-card">
            <div class="card-header">
              <div>
                <h2>{{ detail.itemName || '办件申请' }}</h2>
                <p class="order-no">办件编号：{{ detail.applicationNo }}</p>
              </div>
              <span :class="['status-badge', `s-${detail.status}`]">
                {{ getStatusText(detail.status) }}
              </span>
            </div>

            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">申请人</span>
                <span class="info-value">{{ detail.userName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">申请时间</span>
                <span class="info-value">{{ detail.applyTime || detail.createTime }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">加急</span>
                <span class="info-value">{{ detail.urgency === 1 ? '加急' : '普通' }}</span>
              </div>
              <div class="info-item" v-if="detail.remark">
                <span class="info-label">备注</span>
                <span class="info-value">{{ detail.remark }}</span>
              </div>
            </div>
          </div>

          <!-- 表单数据 -->
          <div class="detail-card" v-if="formDisplay && Object.keys(formDisplay).length > 0">
            <h3>📋 申请信息</h3>
            <div class="form-grid">
              <div class="form-item" v-for="(value, key) in formDisplay" :key="key">
                <span class="form-label">{{ key }}</span>
                <span class="form-value">{{ value }}</span>
              </div>
            </div>
            <!-- 图片 -->
            <div v-if="formImages.length > 0" class="form-images">
              <div v-for="(img, idx) in formImages" :key="idx" class="image-card">
                <img :src="img.url" class="image-thumb" @click="previewImage(img.url)" />
              </div>
            </div>
          </div>

          <!-- 材料清单 -->
          <div class="detail-card" v-if="detail.materials && detail.materials.length > 0">
            <h3>📎 材料清单</h3>
            <div class="material-list">
              <div v-for="(m, i) in detail.materials" :key="i" class="material-item">
                <span class="material-status">{{ m.fileUrl ? '✅' : '⬜' }}</span>
                <span class="material-name">{{ m.materialName }}</span>
                <el-button
                  v-if="m.fileUrl"
                  type="primary" link size="small"
                  @click="previewImage(m.fileUrl)"
                >
                  👁 预览
                </el-button>
              </div>
            </div>
          </div>

          <!-- 审批时间线 -->
          <div class="detail-card">
            <h3>⏱ 审批记录</h3>
            <div v-if="records.length > 0" class="record-timeline">
              <div v-for="(record, index) in records" :key="index" class="record-item">
                <div class="record-dot" :class="{ last: index === records.length - 1 }"></div>
                <div class="record-content">
                  <div class="record-header">
                    <span class="record-action">{{ record.actionText || record.action }}</span>
                    <span class="record-time">{{ record.operTime }}</span>
                  </div>
                  <div class="record-operator" v-if="record.operName">操作人：{{ record.operName }}</div>
                  <div class="record-comment" v-if="record.comment">{{ record.comment }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无审批记录" :image-size="80" />
          </div>

          <!-- 操作 -->
          <div class="detail-actions">
            <el-button @click="router.push('/progress')">返回列表</el-button>
            <el-button
              v-if="detail.status === 5 || detail.status === 3"
              type="primary"
              @click="router.push(`/apply/${detail.itemId}`)"
            >
              重新申请
            </el-button>
            <el-button
              v-if="detail.status === 0"
              type="primary"
              @click="router.push(`/apply/${detail.itemId}`)"
            >
              继续填写
            </el-button>
          </div>
        </template>
      </div>
    </div>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="文件预览" width="600px" append-to-body>
      <div style="text-align: center;">
        <img :src="previewUrl" alt="" style="max-width: 100%; max-height: 70vh; border-radius: 8px;" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getApplicationDetail, getApprovalRecords } from '@/api/approval'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const detail = ref(null)
const records = ref([])

const previewVisible = ref(false)
const previewUrl = ref('')

function previewImage(url) {
  previewUrl.value = url
  previewVisible.value = true
}

const statusTextMap = {
  0: '草稿', 1: '待受理', 2: '审核中', 3: '退回补材料', 4: '审批中', 5: '已驳回', 6: '已通过'
}
function getStatusText(s) { return statusTextMap[s] || '未知' }

// 表单数据（去掉 _uploadImages）
const formDisplay = computed(() => {
  if (!detail.value?.formData) return {}
  try {
    const parsed = JSON.parse(detail.value.formData)
    delete parsed._uploadImages
    // 去掉空值
    const result = {}
    for (const [k, v] of Object.entries(parsed)) {
      if (v !== '' && v != null) result[k] = v
    }
    return result
  } catch { return {} }
})

// 表单里的图片
const formImages = computed(() => {
  if (!detail.value?.formData) return []
  try {
    const parsed = JSON.parse(detail.value.formData)
    const imgs = parsed._uploadImages
    if (!imgs) return []
    const result = []
    for (const [name, urls] of Object.entries(imgs)) {
      (Array.isArray(urls) ? urls : [urls]).forEach(url => result.push({ name, url }))
    }
    return result
  } catch { return [] }
})

onMounted(async () => {
  const id = route.params.id
  if (!id) { router.push('/progress'); return }
  try {
    const res = await getApplicationDetail(id)
    detail.value = res.data

    // 查审批记录
    try {
      const recordRes = await getApprovalRecords(id)
      records.value = recordRes.data || []
    } catch { records.value = [] }
  } catch (e) {
    console.error('加载详情失败:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: #f3f4f6;
}
.page-container { padding: 24px 0 48px; }
.container { max-width: 900px; margin: 0 auto; padding: 0 24px; }
.breadcrumb {
  font-size: 13px; color: #6b7280; margin-bottom: 20px;
  a { color: #3b82f6; text-decoration: none; &:hover { text-decoration: underline; } }
  .separator { margin: 0 8px; color: #d1d5db; }
  .current { color: #1f2937; }
}
.loading-card {
  background: white; border-radius: 12px; padding: 60px; text-align: center;
  .loading-spinner {
    width: 32px; height: 32px; border: 3px solid #e5e7eb; border-top-color: #3b82f6;
    border-radius: 50%; animation: spin 0.8s linear infinite; margin: 0 auto 12px;
  }
}
@keyframes spin { to { transform: rotate(360deg); } }

.detail-card {
  background: white; border-radius: 12px; padding: 24px; margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  h3 { font-size: 16px; margin-bottom: 16px; }
}
.card-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;
  h2 { font-size: 20px; margin-bottom: 4px; }
  .order-no { font-size: 13px; color: #6b7280; }
}
.status-badge {
  padding: 6px 16px; border-radius: 6px; font-size: 13px; font-weight: 500;
  &.s-0, &.s-1 { background: #fef3c7; color: #d97706; }
  &.s-2, &.s-4 { background: #dbeafe; color: #2563eb; }
  &.s-3 { background: #fef3c7; color: #d97706; }
  &.s-5 { background: #fee2e2; color: #dc2626; }
  &.s-6 { background: #d1fae5; color: #059669; }
}
.info-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
}
.info-item {
  display: flex; gap: 8px;
  .info-label { color: #6b7280; font-size: 13px; min-width: 60px; }
  .info-value { color: #1f2937; font-size: 14px; }
}
.form-grid {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
}
.form-item {
  display: flex; gap: 8px;
  .form-label { color: #6b7280; font-size: 13px; min-width: 80px; }
  .form-value { color: #1f2937; font-size: 14px; }
}
.form-images {
  display: flex; flex-wrap: wrap; gap: 12px; margin-top: 16px;
}
.image-card {
  .image-thumb {
    width: 56px; height: 78px; object-fit: cover; border-radius: 4px;
    cursor: pointer; border: 1px solid #e5e7eb;
    &:hover { border-color: #3b82f6; }
  }
}
.material-list { display: flex; flex-direction: column; gap: 12px; }
.material-item {
  display: flex; align-items: center; gap: 12px; padding: 10px 16px;
  background: #f9fafb; border-radius: 8px;
  .material-name { font-size: 14px; color: #1f2937; }
}

.record-timeline { padding-left: 8px; }
.record-item {
  display: flex; gap: 16px; position: relative; padding-bottom: 20px;
  &::before {
    content: ''; position: absolute; left: 5px; top: 16px; bottom: 0; width: 2px; background: #e5e7eb;
  }
  &:last-child::before { display: none; }
}
.record-dot {
  width: 12px; height: 12px; border-radius: 50%; background: #3b82f6; margin-top: 4px;
  flex-shrink: 0; z-index: 1;
  &.last { background: #10b981; }
}
.record-content {
  flex: 1;
}
.record-header {
  display: flex; justify-content: space-between;
  .record-action { font-size: 14px; font-weight: 500; color: #1f2937; }
  .record-time { font-size: 12px; color: #6b7280; }
}
.record-operator {
  font-size: 13px; color: #6b7280; margin-top: 4px;
}
.record-comment {
  font-size: 13px; color: #374151; margin-top: 8px; padding: 8px 12px;
  background: #f3f4f6; border-radius: 6px;
}
.detail-actions {
  text-align: center; margin-top: 24px;
}
</style>
