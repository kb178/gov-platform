<template>
  <div class="progress-page">
    <!-- 页面横幅 -->
    <section class="page-banner">
      <div class="container">
        <h2>办件进度查询</h2>
        <p>实时查看您的办件审批进度</p>
      </div>
    </section>

    <div class="page-container">
      <div class="container">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <div class="search-input-wrap">
            <span class="search-icon">🔍</span>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="输入办件编号或事项名称查询"
              @keyup.enter="handleSearch"
            />
          </div>
          <button class="search-btn" @click="handleSearch">查 询</button>
        </div>

        <!-- 状态筛选 -->
        <div class="status-filter">
          <button
            v-for="status in statusList"
            :key="status.id"
            :class="['status-btn', { active: activeStatus === status.id }]"
            @click="handleStatusChange(status.id)"
          >
            {{ status.name }}
            <span class="count">{{ status.count }}</span>
          </button>
        </div>

        <!-- 办件列表 -->
        <div v-if="filteredProgressList.length > 0" class="progress-list">
          <div v-for="item in filteredProgressList" :key="item.id" class="progress-card">
            <div class="progress-card-header">
              <div>
                <div class="progress-title">{{ item.icon }} {{ item.title }}</div>
                <div class="progress-number">办件编号：{{ item.orderNo }}</div>
              </div>
              <span :class="['progress-status', `status-${item.status}`]">
                {{ item.statusText }}
              </span>
            </div>

            <div class="progress-card-body">
              <div class="progress-info-item">
                <span class="progress-info-label">申请时间</span>
                <span class="progress-info-value">{{ item.applyTime }}</span>
              </div>
              <div class="progress-info-item">
                <span class="progress-info-label">受理部门</span>
                <span class="progress-info-value">{{ item.dept }}</span>
              </div>
              <div v-if="item.status === 'approved'" class="progress-info-item">
                <span class="progress-info-label">办结时间</span>
                <span class="progress-info-value highlight-success">{{ item.completeTime }}</span>
              </div>
              <div v-else-if="item.status === 'rejected'" class="progress-info-item">
                <span class="progress-info-label">驳回时间</span>
                <span class="progress-info-value highlight-danger">{{ item.rejectTime }}</span>
              </div>
              <div v-else class="progress-info-item">
                <span class="progress-info-label">办理时限</span>
                <span class="progress-info-value">{{ item.duration }}</span>
              </div>
              <div v-if="item.currentStep" class="progress-info-item">
                <span class="progress-info-label">当前环节</span>
                <span
                  :class="[
                    'progress-info-value',
                    `highlight-${item.status === 'rejected' ? 'danger' : 'primary'}`
                  ]"
                >
                  {{ item.currentStep }}
                </span>
              </div>
            </div>

            <!-- 进度时间线 -->
            <div v-if="item.timeline && item.timeline.length > 0" class="progress-timeline">
              <div class="timeline-header">
                <span class="timeline-title">办理进度</span>
                <span class="timeline-toggle" @click="toggleTimeline(item.id)">
                  {{ expandedTimeline.includes(item.id) ? '收起 ▲' : '展开 ▼' }}
                </span>
              </div>
              <div v-show="expandedTimeline.includes(item.id)" class="timeline-steps">
                <div
                  v-for="(step, index) in item.timeline"
                  :key="index"
                  :class="['timeline-step', step.status]"
                >
                  <div class="timeline-dot">
                    {{ step.status === 'completed' ? '✓' : index + 1 }}
                  </div>
                  <div class="timeline-label">{{ step.name }}</div>
                  <div class="timeline-time">{{ step.time || '' }}</div>
                </div>
              </div>
            </div>

            <!-- 驳回/退回原因 -->
            <div v-if="item.status === 'rejected' || item.status === 'returned'" class="reject-reason">
              <span class="reject-icon">{{ item.status === 'rejected' ? '❌' : '↩️' }}</span>
              <span class="reject-text">{{ item.statusText }}，请补充材料后重新提交</span>
            </div>

            <div class="progress-card-footer">
              <span v-if="item.status === 'approved'" class="footer-text success"> ✅ 已办结 </span>
              <span v-else-if="item.status === 'rejected'" class="footer-text danger"> 已驳回 </span>
              <span v-else-if="item.status === 'returned'" class="footer-text danger"> 退回补材料 </span>
              <span v-else-if="item.status === 'draft'" class="footer-text"> 草稿未提交 </span>
              <span v-else class="footer-text"> {{ item.statusText }} </span>
              <div class="progress-actions">
                <button class="btn-action btn-detail" @click="handleDetail(item)">查看详情</button>
                <button
                  v-if="item.status === 'rejected' || item.status === 'returned'"
                  class="btn-action btn-reapply"
                  @click="handleReapply(item)"
                >
                  重新申请
                </button>
                <button
                  v-if="item.status === 'draft'"
                  class="btn-action btn-reapply"
                  @click="handleReapply(item)"
                >
                  继续填写
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">📋</div>
          <div class="empty-title">暂无办件记录</div>
          <div class="empty-desc">您还没有提交过办件申请，去事项办理页面看看吧</div>
          <button class="btn-go" @click="router.push('/items')">去办事</button>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination-wrap">
          <div class="pagination">
            <span
              :class="['page-btn', { disabled: currentPage === 1 }]"
              @click="handlePageChange(currentPage - 1)"
            >
              ‹
            </span>
            <span
              v-for="page in displayPages"
              :key="page"
              :class="['page-btn', { active: currentPage === page }]"
              @click="handlePageChange(page)"
            >
              {{ page }}
            </span>
            <span
              :class="['page-btn', { disabled: currentPage === totalPages }]"
              @click="handlePageChange(currentPage + 1)"
            >
              ›
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApplications, getApplicationDetail } from '@/api/approval'

const router = useRouter()

// ========== 搜索 ==========
const searchKeyword = ref('')

const handleSearch = () => {
  loadData()
}

// ========== 状态筛选 ==========
const activeStatus = ref('all')

// 后端状态：0草稿 1待受理 2审核中 3退回 4审批中 5已驳回 6已通过
const statusMap = {
  0: 'draft',
  1: 'pending',
  2: 'processing',
  3: 'returned',
  4: 'processing',
  5: 'rejected',
  6: 'approved'
}

const statusTextMap = {
  0: '草稿',
  1: '待受理',
  2: '审核中',
  3: '退回补材料',
  4: '审批中',
  5: '已驳回',
  6: '已通过'
}

const statusList = computed(() => {
  const all = progressList.value
  return [
    { id: 'all', name: '全部', count: all.length },
    { id: 'pending', name: '待受理', count: all.filter(i => i.status === 'pending').length },
    { id: 'processing', name: '审核中', count: all.filter(i => i.status === 'processing').length },
    { id: 'approved', name: '已办结', count: all.filter(i => i.status === 'approved').length },
    { id: 'rejected', name: '已驳回', count: all.filter(i => i.status === 'rejected').length }
  ]
})

const handleStatusChange = statusId => {
  activeStatus.value = statusId
  currentPage.value = 1
}

const getStatusText = item => item.statusText || ''

// ========== 加载数据 ==========
const loading = ref(false)
const progressList = ref([])
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    // 不传 status 查全部（status 传 undefined 后端查所有）
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    // 状态筛选映射到后端 status 参数
    const statusReverseMap = {
      pending: 1,
      processing: 2,
      approved: 6,
      rejected: 5
    }
    if (activeStatus.value !== 'all') {
      params.status = statusReverseMap[activeStatus.value]
    }

    const res = await getMyApplications(params)
    const records = res.data?.records || []
    total.value = res.data?.total || 0

    // 转换为页面需要的格式
    progressList.value = records.map(r => ({
      id: r.applicationId,
      itemId: r.itemId,
      icon: '📄',
      title: r.itemName || '办件申请',
      orderNo: r.applicationNo,
      status: statusMap[r.status] || 'pending',
      statusText: statusTextMap[r.status] || '未知',
      applyTime: r.applyTime || r.createTime || '',
      dept: r.deptName || '',
      duration: r.processTime || '',
      currentStep: statusTextMap[r.status] || '',
      createTime: r.createTime
    }))
  } catch (e) {
    console.error('加载申请列表失败:', e)
  } finally {
    loading.value = false
  }
}

// 筛选后的列表
const filteredProgressList = computed(() => {
  let list = progressList.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(
      item =>
        item.orderNo?.toLowerCase().includes(keyword) || item.title?.toLowerCase().includes(keyword)
    )
  }

  return list
})

onMounted(() => {
  loadData()
})

// ========== 时间线展开/收起 ==========
const expandedTimeline = ref([1]) // 默认第一个展开

const toggleTimeline = id => {
  const index = expandedTimeline.value.indexOf(id)
  if (index > -1) {
    expandedTimeline.value.splice(index, 1)
  } else {
    expandedTimeline.value.push(id)
  }
}

// ========== 分页（服务端） ==========
const currentPage = ref(1)
const pageSize = ref(10)

const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

const displayPages = computed(() => {
  const pages = []
  const t = totalPages.value
  const current = currentPage.value

  if (t <= 5) {
    for (let i = 1; i <= t; i++) pages.push(i)
  } else {
    if (current <= 3) {
      for (let i = 1; i <= 5; i++) pages.push(i)
    } else if (current >= t - 2) {
      for (let i = t - 4; i <= t; i++) pages.push(i)
    } else {
      for (let i = current - 2; i <= current + 2; i++) pages.push(i)
    }
  }
  return pages
})

const handlePageChange = page => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadData()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// ========== 操作 ==========
const handleDetail = item => {
  router.push(`/progress/${item.id}`)
}

const handleReapply = item => {
  // 草稿 → 继续填写（带 draftId 自动恢复）；退回/驳回 → 重新申请
  if (item.status === 'draft') {
    router.push(`/apply/${item.itemId}?draftId=${item.id}`)
  } else {
    router.push(`/apply/${item.itemId}`)
  }
}
</script>

<style lang="scss" scoped>
.progress-page {
  min-height: 100vh;
  background: #f3f4f6;
}

.page-banner {
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  padding: 32px 0;
  color: white;

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

.page-container {
  padding-bottom: 48px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 搜索栏 */
.search-bar {
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  margin-top: -24px;
  position: relative;
  z-index: 10;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 16px;
}

.search-input-wrap {
  flex: 1;
  position: relative;

  .search-icon {
    position: absolute;
    left: 14px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 18px;
  }

  input {
    width: 100%;
    height: 44px;
    padding: 0 16px 0 44px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s;

    &:focus {
      border-color: #3b82f6;
    }

    &::placeholder {
      color: #9ca3af;
    }
  }
}

.search-btn {
  width: 100px;
  height: 44px;
  background: #1e40af;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #1e3a8a;
  }
}

/* 状态筛选 */
.status-filter {
  display: flex;
  gap: 12px;
  margin: 24px 0;
  flex-wrap: wrap;
}

.status-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
  border: 1px solid #e5e7eb;
  color: #4b5563;

  &:hover {
    border-color: #93c5fd;
    color: #1e40af;
  }

  &.active {
    background: #1e40af;
    border-color: #1e40af;
    color: white;
  }

  .count {
    display: inline-block;
    margin-left: 6px;
    padding: 0 6px;
    height: 18px;
    line-height: 18px;
    border-radius: 9px;
    font-size: 12px;
    background: rgba(0, 0, 0, 0.1);
  }

  &.active .count {
    background: rgba(255, 255, 255, 0.3);
  }
}

/* 办件列表 */
.progress-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid transparent;

  &:hover {
    border-color: #93c5fd;
    box-shadow: 0 4px 12px rgba(30, 64, 175, 0.1);
  }
}

.progress-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.progress-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-number {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.progress-status {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;

  &.status-pending {
    background: #fef3c7;
    color: #d97706;
  }

  &.status-processing {
    background: #dbeafe;
    color: #2563eb;
  }

  &.status-approved {
    background: #d1fae5;
    color: #059669;
  }

  &.status-rejected {
    background: #fee2e2;
    color: #dc2626;
  }

  &.status-returned {
    background: #fef3c7;
    color: #d97706;
  }

  &.status-draft {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.progress-card-body {
  display: flex;
  gap: 32px;
  padding: 16px 0;
  border-top: 1px solid #f3f4f6;
  border-bottom: 1px solid #f3f4f6;
}

.progress-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.progress-info-label {
  font-size: 12px;
  color: #6b7280;
}

.progress-info-value {
  font-size: 14px;
  color: #1f2937;

  &.highlight-primary {
    color: #2563eb;
  }

  &.highlight-success {
    color: #059669;
  }

  &.highlight-danger {
    color: #dc2626;
  }
}

/* 进度时间线 */
.progress-timeline {
  margin-top: 16px;
  padding-top: 16px;
}

.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.timeline-toggle {
  font-size: 13px;
  color: #3b82f6;
  cursor: pointer;
  user-select: none;

  &:hover {
    color: #1e40af;
  }
}

.timeline-steps {
  display: flex;
  align-items: center;
  gap: 0;
  overflow-x: auto;
  padding-bottom: 8px;
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
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

  &.completed:not(:last-child)::after {
    background: #10b981;
  }

  &.active:not(:last-child)::after {
    background: linear-gradient(90deg, #10b981 50%, #e5e7eb 50%);
  }
}

.timeline-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  position: relative;
  z-index: 1;
  margin-bottom: 8px;
}

.timeline-step.completed .timeline-dot {
  background: #10b981;
  color: white;
}

.timeline-step.active .timeline-dot {
  background: #1e40af;
  color: white;
  box-shadow: 0 0 0 4px rgba(30, 64, 175, 0.2);
}

.timeline-label {
  font-size: 12px;
  color: #6b7280;
  text-align: center;
}

.timeline-step.completed .timeline-label,
.timeline-step.active .timeline-label {
  color: #1f2937;
  font-weight: 500;
}

.timeline-time {
  font-size: 11px;
  color: #6b7280;
  margin-top: 4px;
}

/* 驳回原因 */
.reject-reason {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef2f2;
  border-radius: 8px;
  margin-top: 16px;
}

.reject-icon {
  font-size: 16px;
}

.reject-text {
  font-size: 14px;
  color: #dc2626;
}

/* 操作按钮 */
.progress-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.footer-text {
  font-size: 13px;
  color: #6b7280;

  &.success {
    color: #059669;
  }

  &.danger {
    color: #dc2626;
  }
}

.progress-actions {
  display: flex;
  gap: 12px;
}

.btn-action {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-detail {
  background: #eff6ff;
  color: #1e40af;

  &:hover {
    background: #dbeafe;
  }
}

.btn-urge {
  background: #fef3c7;
  color: #d97706;

  &:hover {
    background: #fde68a;
  }
}

.btn-download {
  background: #d1fae5;
  color: #059669;

  &:hover {
    background: #a7f3d0;
  }
}

.btn-reapply {
  background: #1e40af;
  color: white;

  &:hover {
    background: #1e3a8a;
  }
}

/* 空状态 */
.empty-state {
  background: white;
  border-radius: 12px;
  padding: 80px 40px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  .empty-icon {
    font-size: 64px;
    margin-bottom: 20px;
  }

  .empty-title {
    font-size: 16px;
    color: #1f2937;
    margin-bottom: 8px;
  }

  .empty-desc {
    font-size: 14px;
    color: #6b7280;
    margin-bottom: 24px;
  }

  .btn-go {
    padding: 10px 24px;
    background: #1e40af;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;

    &:hover {
      background: #1e3a8a;
    }
  }
}

/* 分页 */
.pagination-wrap {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.pagination {
  display: flex;
  gap: 8px;
}

.page-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
  border: 1px solid #e5e7eb;
  color: #4b5563;

  &:hover:not(.disabled) {
    border-color: #93c5fd;
    color: #1e40af;
  }

  &.active {
    background: #1e40af;
    border-color: #1e40af;
    color: white;
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
  }

  .search-btn {
    width: 100%;
  }

  .status-filter {
    gap: 8px;
  }

  .status-btn {
    padding: 8px 16px;
    font-size: 13px;
  }

  .progress-card-body {
    flex-wrap: wrap;
    gap: 16px;
  }

  .progress-card-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .progress-actions {
    width: 100%;

    .btn-action {
      flex: 1;
    }
  }
}
</style>
