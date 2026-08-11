<template>
  <div class="dashboard-page">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-info">
        <h2>👋 欢迎回来，{{ userStore.nickname || '管理员' }}</h2>
        <p>今天是 {{ todayStr }}</p>
        <div class="welcome-time">上次登录时间：2024-01-15 18:30:22</div>
      </div>
      <div class="welcome-actions">
        <el-button class="welcome-btn" @click="$router.push('/approval/pending')">待我审批</el-button>
        <el-button class="welcome-btn" @click="$router.push('/statistics/overview')">数据统计</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon blue">📋</div>
        <div class="stat-content">
          <div class="stat-value">1,258</div>
          <div class="stat-label">本月办件总量</div>
          <div class="stat-trend up">↑ 12.5% 较上月</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">⏳</div>
        <div class="stat-content">
          <div class="stat-value">48</div>
          <div class="stat-label">待审批件数</div>
          <div class="stat-trend down">↓ 5.2% 较昨日</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">✅</div>
        <div class="stat-content">
          <div class="stat-value">96.8%</div>
          <div class="stat-label">按时办结率</div>
          <div class="stat-trend up">↑ 2.1% 较上月</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">⏱️</div>
        <div class="stat-content">
          <div class="stat-value">2.5天</div>
          <div class="stat-label">平均审批时长</div>
          <div class="stat-trend up">↑ 0.3天 较上月</div>
        </div>
      </div>
    </div>

    <!-- 待办事项 + 快捷入口 -->
    <div class="todo-section">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">📋 待办事项</h3>
          <el-link type="primary" :underline="false" @click="$router.push('/approval/pending')">查看全部</el-link>
        </div>
        <div class="todo-list">
          <div v-for="item in todoList" :key="item.id" class="todo-item" @click="$router.push('/approval/pending')">
            <div class="todo-icon" :class="item.type">{{ item.icon }}</div>
            <div class="todo-content">
              <div class="todo-title">{{ item.title }}</div>
              <div class="todo-meta">申请人：{{ item.applicant }} | 提交时间：{{ item.time }}</div>
            </div>
            <el-tag v-if="item.urgent" type="danger" size="small" effect="dark">加急</el-tag>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">⚡ 快捷入口</h3>
        </div>
        <div class="quick-links">
          <div v-for="link in quickLinks" :key="link.text" class="quick-link" @click="$router.push(link.path)">
            <span class="quick-link-icon">{{ link.icon }}</span>
            <span class="quick-link-text">{{ link.text }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">📊 办件趋势</h3>
          <div class="chart-legend">
            <span class="legend-item"><span class="legend-dot blue"></span>本月</span>
            <span class="legend-item"><span class="legend-dot gray"></span>上月</span>
          </div>
        </div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">🏢 部门办件排行</h3>
        </div>
        <div ref="deptChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 最近审批 + 系统公告 -->
    <div class="recent-section">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">📝 最近审批</h3>
          <el-link type="primary" :underline="false" @click="$router.push('/approval/done')">查看全部</el-link>
        </div>
        <el-table :data="recentApprovals" style="width: 100%">
          <el-table-column label="申请人" min-width="120">
            <template #default="{ row }">
              <div class="applicant-info">
                <div class="applicant-avatar">{{ row.applicant.charAt(0) }}</div>
                <span>{{ row.applicant }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="item" label="事项名称" min-width="160" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.statusType" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="审批时间" width="160" />
        </el-table>
      </div>

      <div class="card">
        <div class="card-header">
          <h3 class="card-title">📢 系统公告</h3>
          <el-link type="primary" :underline="false" @click="$router.push('/message/notice')">管理公告</el-link>
        </div>
        <div class="notice-list">
          <div v-for="notice in notices" :key="notice.id" class="notice-item">
            <el-tag :type="notice.important ? 'danger' : 'info'" size="small" class="notice-tag">
              {{ notice.important ? '重要' : '通知' }}
            </el-tag>
            <span class="notice-title">{{ notice.title }}</span>
            <span class="notice-date">{{ notice.date }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 今天日期
const todayStr = computed(() => {
  const d = new Date()
  const week = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${week[d.getDay()]}`
})

// 待办事项
const todoList = ref([
  { id: 1, icon: '🚨', type: 'urgent', title: '营业执照申请 - 海南XX科技有限公司', applicant: '张三', time: '2024-01-16 09:30', urgent: true },
  { id: 2, icon: '⏳', type: 'pending', title: '居民身份证换领 - 李四', applicant: '李四', time: '2024-01-16 10:15', urgent: false },
  { id: 3, icon: '📝', type: 'review', title: '食品经营许可证申请 - 海口XX餐饮店', applicant: '王五', time: '2024-01-16 11:00', urgent: false },
  { id: 4, icon: '⏳', type: 'pending', title: '不动产登记 - 赵六', applicant: '赵六', time: '2024-01-16 14:20', urgent: false },
  { id: 5, icon: '📝', type: 'review', title: '公积金提取申请 - 钱七', applicant: '钱七', time: '2024-01-16 15:45', urgent: false }
])

// 快捷入口
const quickLinks = ref([
  { icon: '📋', text: '待我审批', path: '/approval/pending' },
  { icon: '📝', text: '事项管理', path: '/item/list' },
  { icon: '📈', text: '数据统计', path: '/statistics/overview' },
  { icon: '👥', text: '用户管理', path: '/system/user' }
])

// 最近审批
const recentApprovals = ref([
  { applicant: '张三', item: '营业执照变更', status: '已通过', statusType: 'success', time: '2024-01-15 16:30' },
  { applicant: '李四', item: '食品经营许可证', status: '已驳回', statusType: 'danger', time: '2024-01-15 15:20' },
  { applicant: '王五', item: '居民身份证办理', status: '已通过', statusType: 'success', time: '2024-01-15 14:10' },
  { applicant: '赵六', item: '不动产登记', status: '待审核', statusType: 'warning', time: '2024-01-15 11:45' }
])

// 系统公告
const notices = ref([
  { id: 1, title: '系统升级通知：1月20日凌晨维护', date: '01-16', important: true },
  { id: 2, title: '关于2024年春节假期值班安排', date: '01-15', important: false },
  { id: 3, title: '新事项办理流程上线通知', date: '01-14', important: false },
  { id: 4, title: '1月份办件质量分析报告', date: '01-13', important: false },
  { id: 5, title: '工作人员培训通知', date: '01-12', important: false }
])

// 图表
const trendChartRef = ref(null)
const deptChartRef = ref(null)
let trendChart = null
let deptChart = null

function initTrendChart() {
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: ['1月1日', '1月5日', '1月10日', '1月15日', '1月20日', '1月25日', '1月30日'] },
    yAxis: { type: 'value' },
    series: [
      {
        name: '本月', type: 'line', smooth: true,
        data: [120, 132, 101, 134, 90, 230, 210],
        areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(30,64,175,0.3)' }, { offset: 1, color: 'rgba(30,64,175,0.05)' }] } },
        lineStyle: { color: '#1E40AF', width: 2 },
        itemStyle: { color: '#1E40AF' }
      },
      {
        name: '上月', type: 'line', smooth: true,
        data: [80, 102, 91, 114, 100, 180, 160],
        lineStyle: { color: '#93C5FD', width: 2, type: 'dashed' },
        itemStyle: { color: '#93C5FD' }
      }
    ]
  })
}

function initDeptChart() {
  deptChart = echarts.init(deptChartRef.value)
  deptChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: ['教育局', '人社局', '公积金中心', '市场监管局', '自然资源局', '公安局'] },
    series: [{
      name: '办件量', type: 'bar', data: [180, 230, 290, 380, 450, 520], barWidth: 20,
      itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0, colorStops: [{ offset: 0, color: '#3B82F6' }, { offset: 1, color: '#1E40AF' }] }, borderRadius: [0, 4, 4, 0] }
    }]
  })
}

function handleResize() {
  trendChart?.resize()
  deptChart?.resize()
}

onMounted(() => {
  initTrendChart()
  initDeptChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  deptChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  // 欢迎区域
  .welcome-section {
    background: linear-gradient(135deg, #1E40AF 0%, #3B82F6 100%);
    border-radius: 12px;
    padding: 32px;
    color: white;
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .welcome-info {
    h2 { font-size: 20px; font-weight: 600; margin-bottom: 8px; }
    p { opacity: 0.9; font-size: 14px; }
  }

  .welcome-time { font-size: 13px; opacity: 0.8; margin-top: 8px; }

  .welcome-actions {
    display: flex;
    gap: 12px;
  }

  .welcome-btn {
    height: 36px;
    padding: 0 20px;
    background: rgba(255, 255, 255, 0.2);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 8px;
    font-size: 14px;
    &:hover { background: rgba(255, 255, 255, 0.3); }
  }

  // 统计卡片
  .stat-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;
  }

  .stat-card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    flex-shrink: 0;
    &.blue { background: #EFF6FF; }
    &.orange { background: #FFF7ED; }
    &.green { background: #F0FDF4; }
    &.purple { background: #FAF5FF; }
  }

  .stat-value { font-size: 24px; font-weight: 700; color: #1F2937; }
  .stat-label { font-size: 13px; color: #6B7280; margin-top: 4px; }
  .stat-trend {
    font-size: 12px; margin-top: 4px;
    &.up { color: #22C55E; }
    &.down { color: #EF4444; }
  }

  // 待办 + 快捷入口
  .todo-section {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 24px;
    margin-bottom: 24px;
  }

  .card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
  }

  .card-title { font-size: 16px; font-weight: 600; color: #1F2937; }

  .todo-list { display: flex; flex-direction: column; gap: 12px; }

  .todo-item {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: #F9FAFB;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    &:hover { background: #EFF6FF; }
  }

  .todo-icon {
    width: 40px; height: 40px; border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    font-size: 20px; margin-right: 12px; flex-shrink: 0;
    &.urgent { background: #FEE2E2; }
    &.pending { background: #FEF3C7; }
    &.review { background: #DBEAFE; }
  }

  .todo-content { flex: 1; min-width: 0; }
  .todo-title { font-size: 14px; font-weight: 500; color: #1F2937; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .todo-meta { font-size: 12px; color: #6B7280; margin-top: 4px; }

  // 快捷入口
  .quick-links { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }

  .quick-link {
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    padding: 20px; background: #F9FAFB; border-radius: 8px;
    cursor: pointer; transition: all 0.2s;
    &:hover { background: #EFF6FF; transform: translateY(-2px); }
  }

  .quick-link-icon { font-size: 28px; margin-bottom: 8px; }
  .quick-link-text { font-size: 13px; color: #1F2937; }

  // 图表区域
  .charts-section {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
    margin-bottom: 24px;
  }

  .chart-card {
    background: white; border-radius: 12px; padding: 24px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  }

  .chart-header {
    display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;
  }

  .chart-title { font-size: 16px; font-weight: 600; color: #1F2937; }

  .chart-legend { display: flex; gap: 20px; }
  .legend-item { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #6B7280; }
  .legend-dot { width: 8px; height: 8px; border-radius: 50%; &.blue { background: #1E40AF; } &.gray { background: #93C5FD; } }

  .chart-container { height: 300px; }

  // 最近审批 + 公告
  .recent-section {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 24px;
  }

  .applicant-info { display: flex; align-items: center; gap: 8px; }
  .applicant-avatar {
    width: 28px; height: 28px; border-radius: 50%; background: #DBEAFE;
    display: flex; align-items: center; justify-content: center;
    font-size: 12px; color: #1E40AF; font-weight: 600;
  }

  .notice-list { display: flex; flex-direction: column; }
  .notice-item {
    display: flex; align-items: center; padding: 12px 0;
    border-bottom: 1px solid #F3F4F6; cursor: pointer;
    &:last-child { border-bottom: none; }
    &:hover { color: #1E40AF; }
  }
  .notice-tag { margin-right: 12px; flex-shrink: 0; }
  .notice-title { flex: 1; font-size: 13px; color: #1F2937; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .notice-date { font-size: 12px; color: #9CA3AF; margin-left: 12px; flex-shrink: 0; }
}
</style>
