<template>
  <div class="message-page">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>消息中心</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 页面标题 -->
    <div class="page-banner">
      <h2>消息中心</h2>
      <p>查看系统通知和办件进度提醒</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon blue">📬</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">全部消息</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">📨</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.unread }}</div>
          <div class="stat-label">未读消息</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">✅</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.read }}</div>
          <div class="stat-label">已读消息</div>
        </div>
      </div>
    </div>

    <!-- 筛选和操作栏 -->
    <div class="action-bar">
      <div class="filter-tabs">
        <span
          v-for="tab in tabs"
          :key="tab.value"
          class="filter-tab"
          :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          {{ tab.label }}
        </span>
      </div>
      <div class="action-buttons">
        <el-button @click="markAllRead">
          <el-icon><Check /></el-icon>
          <span>全部已读</span>
        </el-button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-list">
      <div
        v-for="message in filteredMessages"
        :key="message.id"
        class="message-card"
        :class="{ unread: !message.isRead }"
        @click="showDetail(message)"
      >
        <div class="message-header">
          <div class="message-type">
            <div class="message-type-icon" :class="'type-' + message.type">
              <span>{{ getTypeIcon(message.type) }}</span>
            </div>
            <span class="message-type-text" :class="'type-' + message.type">
              {{ getTypeLabel(message.type) }}
            </span>
          </div>
          <span class="message-time">{{ message.time }}</span>
        </div>
        <div class="message-title">
          <span v-if="!message.isRead" class="unread-dot"></span>
          {{ message.title }}
        </div>
        <div class="message-content">{{ message.content }}</div>
        <div class="message-footer">
          <span class="message-link" @click.stop="showDetail(message)">查看详情 →</span>
          <div class="message-actions">
            <span v-if="!message.isRead" class="msg-btn" @click.stop="markRead(message)">标为已读</span>
            <span class="msg-btn" @click.stop="deleteMessage(message)">删除</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="filteredMessages.length === 0" class="empty-state">
      <div class="empty-icon">📭</div>
      <div class="empty-title">暂无消息</div>
      <div class="empty-desc">您还没有相关消息通知</div>
    </div>

    <!-- 分页 -->
    <div v-if="filteredMessages.length > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="filteredMessages.length"
        layout="prev, pager, next"
      />
    </div>

    <!-- 消息详情弹窗 -->
    <el-dialog
      v-model="showModal"
      title="消息详情"
      width="560px"
    >
      <div class="modal-message-info">
        <span>📅 {{ currentMessage?.time }}</span>
        <span>📂 {{ getTypeLabel(currentMessage?.type) }}</span>
      </div>
      <div class="modal-message-content">
        <p>{{ currentMessage?.content }}</p>
      </div>
      <template #footer>
        <el-button @click="showModal = false">关闭</el-button>
        <el-button v-if="currentMessage?.type === 'approval'" type="primary" @click="goToProgress">
          查看办件进度
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Check } from '@element-plus/icons-vue'

const router = useRouter()

// 状态
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const showModal = ref(false)
const currentMessage = ref(null)

// 统计数据
const stats = ref({
  total: 6,
  unread: 3,
  read: 3
})

// 标签页
const tabs = [
  { label: '全部', value: 'all' },
  { label: '系统通知', value: 'system' },
  { label: '审批通知', value: 'approval' },
  { label: '证照通知', value: 'license' },
  { label: '公告', value: 'notice' }
]

// 消息列表
const messages = ref([
  {
    id: 1,
    type: 'approval',
    title: '您的居民身份证换领申请已受理',
    content: '尊敬的用户，您提交的"居民身份证换领"申请（办件编号：BJ20240115001234）已由海口市公安局户政大厅受理，预计5个工作日内完成审核，请耐心等待。',
    time: '2024-01-15 16:30',
    isRead: false
  },
  {
    id: 2,
    type: 'license',
    title: '您的居住证即将到期，请及时续签',
    content: '尊敬的用户，您的居住证（证照号：460100202301...）将于2024年06月01日到期，请在到期前30日内办理续签手续，以免影响正常使用。',
    time: '2024-01-15 10:00',
    isRead: false
  },
  {
    id: 3,
    type: 'notice',
    title: '关于2024年春节假期政务服务大厅放假通知',
    content: '根据国务院办公厅通知精神，2024年春节假期安排如下：2月10日至17日放假调休，共8天。2月4日（星期日）、2月18日（星期日）上班。放假期间，线上服务正常运行。',
    time: '2024-01-14 09:00',
    isRead: false
  },
  {
    id: 4,
    type: 'approval',
    title: '您的不动产登记申请已办结',
    content: '尊敬的用户，您提交的"不动产登记"申请（办件编号：BJ20240110003456）已审核通过并办结，请登录平台下载电子证照。',
    time: '2024-01-12 16:30',
    isRead: true
  },
  {
    id: 5,
    type: 'system',
    title: '实名认证成功',
    content: '恭喜您，实名认证已通过！现在您可以享受完整的政务服务功能，包括在线申请、进度查询、证照管理等。',
    time: '2024-01-10 14:00',
    isRead: true
  },
  {
    id: 6,
    type: 'approval',
    title: '您的食品经营许可证申请被驳回',
    content: '尊敬的用户，您提交的"食品经营许可证"申请（办件编号：BJ20240108007890）因材料不全被驳回，请补充相关材料后重新提交申请。',
    time: '2024-01-09 15:00',
    isRead: true
  }
])

// 筛选后的消息
const filteredMessages = computed(() => {
  if (activeTab.value === 'all') return messages.value
  return messages.value.filter(m => m.type === activeTab.value)
})

// 获取类型图标
const getTypeIcon = (type) => {
  const icons = {
    system: '⚙️',
    approval: '✅',
    license: '📜',
    notice: '📢'
  }
  return icons[type] || '📌'
}

// 获取类型标签
const getTypeLabel = (type) => {
  const labels = {
    system: '系统通知',
    approval: '审批通知',
    license: '证照通知',
    notice: '系统公告'
  }
  return labels[type] || '通知'
}

// 显示详情
const showDetail = (message) => {
  currentMessage.value = message
  if (!message.isRead) {
    message.isRead = true
    stats.value.unread--
    stats.value.read++
  }
  showModal.value = true
}

// 标为已读
const markRead = (message) => {
  if (!message.isRead) {
    message.isRead = true
    stats.value.unread--
    stats.value.read++
  }
}

// 全部已读
const markAllRead = () => {
  messages.value.forEach(m => {
    if (!m.isRead) {
      m.isRead = true
    }
  })
  stats.value.unread = 0
  stats.value.read = stats.value.total
}

// 删除消息
const deleteMessage = (message) => {
  if (confirm('确定要删除这条消息吗？')) {
    const index = messages.value.findIndex(m => m.id === message.id)
    if (index > -1) {
      messages.value.splice(index, 1)
      stats.value.total--
      if (!message.isRead) {
        stats.value.unread--
      } else {
        stats.value.read--
      }
    }
  }
}

// 跳转到进度查询
const goToProgress = () => {
  showModal.value = false
  router.push('/progress')
}
</script>

<style lang="scss" scoped>
.message-page {
  padding-bottom: 48px;
}

/* 面包屑导航 */
.breadcrumb {
  margin-bottom: 24px;
}

/* 页面标题 */
.page-banner {
  background: linear-gradient(135deg, #1E40AF 0%, #3B82F6 100%);
  padding: 32px;
  border-radius: 12px;
  color: white;
  margin-bottom: 32px;

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

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;

  &.blue { background: var(--el-color-primary-light-9); }
  &.orange { background: var(--el-color-warning-light-9); }
  &.green { background: var(--el-color-success-light-9); }
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

/* 筛选和操作栏 */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
  border: 1px solid var(--el-border-color);
  color: var(--el-text-color-regular);

  &:hover {
    border-color: var(--el-color-primary-light-5);
    color: var(--el-color-primary);
  }

  &.active {
    background: var(--el-color-primary);
    border-color: var(--el-color-primary);
    color: white;
  }
}

.action-buttons {
  display: flex;
  gap: 12px;
}

/* 消息列表 */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s;
  border-left: 4px solid transparent;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &.unread {
    border-left-color: var(--el-color-primary);
    background: #FAFBFF;
  }
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.message-type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-type-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.message-type-text {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.type-system {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.type-approval {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success);
}

.type-notice {
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning);
}

.type-license {
  background: var(--el-color-info-light-9);
  color: var(--el-color-info);
}

.message-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.message-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.message-card.unread .message-title {
  font-weight: 600;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--el-color-primary);
  display: inline-block;
  margin-right: 8px;
}

.message-content {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.message-link {
  font-size: 13px;
  color: var(--el-color-primary);
  cursor: pointer;

  &:hover {
    color: var(--el-color-primary-light-3);
  }
}

.message-actions {
  display: flex;
  gap: 12px;
}

.msg-btn {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
    color: var(--el-text-color-primary);
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
    color: var(--el-text-color-primary);
    margin-bottom: 8px;
  }

  .empty-desc {
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }
}

/* 分页 */
.pagination-wrap {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

/* 弹窗内容 */
.modal-message-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.modal-message-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--el-text-color-regular);
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .action-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
