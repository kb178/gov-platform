<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">流程监控</h1>
      <span class="desc">实时监控办件流转状态与部门工作情况</span>
    </div>

    <!-- 顶部指标 -->
    <div class="metric-cards">
      <div class="metric-card" v-for="m in metrics" :key="m.label">
        <div class="metric-value" :style="{ color: m.color }">{{ m.value }}</div>
        <div class="metric-label">{{ m.label }}</div>
        <div class="metric-trend" :class="m.trend > 0 ? 'up' : 'down'">
          {{ m.trend > 0 ? '↑' : '↓' }} {{ Math.abs(m.trend) }}%
        </div>
      </div>
    </div>

    <div class="monitor-grid">
      <!-- 左侧：在办件列表 -->
      <div class="monitor-left">
        <div class="panel">
          <div class="panel-header">
            <h3>在办件追踪</h3>
            <div class="panel-actions">
              <el-radio-group v-model="listFilter" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="timeout">超时预警</el-radio-button>
                <el-radio-button label="urgent">加急</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="process-list">
            <div
              v-for="item in filteredProcesses"
              :key="item.caseNo"
              :class="['process-item', { timeout: item.timeout, urgent: item.urgent }]"
              @click="showProcessDetail(item)"
            >
              <div class="process-header">
                <span class="process-no">{{ item.caseNo }}</span>
                <el-tag :type="item.timeout ? 'danger' : item.urgent ? 'warning' : 'primary'" size="small" effect="plain">
                  {{ item.timeout ? '已超时' : item.urgent ? '加急' : '正常' }}
                </el-tag>
              </div>
              <div class="process-name">{{ item.itemName }}</div>
              <div class="process-meta">
                <span>{{ item.applicant }}</span>
                <span>{{ item.currentNode }}</span>
              </div>
              <div class="process-progress">
                <el-steps :active="item.stepActive" finish-status="success" simple>
                  <el-step v-for="(node, i) in item.nodes" :key="i" :title="node" />
                </el-steps>
              </div>
              <div class="process-footer">
                <span class="process-time">提交：{{ item.submitTime }}</span>
                <span :class="['process-remain', item.timeout ? 'danger' : item.urgent ? 'warning' : '']">
                  {{ item.remainText }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：统计 -->
      <div class="monitor-right">
        <!-- 部门办件量 -->
        <div class="panel">
          <div class="panel-header"><h3>部门办件量排行</h3></div>
          <div class="dept-list">
            <div class="dept-item" v-for="(dept, i) in deptRanking" :key="dept.name">
              <span :class="['rank', i < 3 ? 'top' : '']">{{ i + 1 }}</span>
              <span class="dept-name">{{ dept.name }}</span>
              <div class="dept-bar">
                <div class="dept-bar-fill" :style="{ width: dept.percent + '%' }"></div>
              </div>
              <span class="dept-count">{{ dept.count }}</span>
            </div>
          </div>
        </div>

        <!-- 超时统计 -->
        <div class="panel">
          <div class="panel-header"><h3>超时预警</h3></div>
          <div class="timeout-stats">
            <div class="timeout-item" v-for="t in timeoutStats" :key="t.label">
              <div class="timeout-dot" :style="{ background: t.color }"></div>
              <span class="timeout-label">{{ t.label }}</span>
              <span class="timeout-value">{{ t.value }} 件</span>
            </div>
          </div>
          <div class="timeout-alert" v-if="timeoutStats[0].value > 0">
            <el-icon><WarningFilled /></el-icon>
            <span>有 {{ timeoutStats[0].value }} 件已超时，请及时处理！</span>
          </div>
        </div>

        <!-- 今日动态 -->
        <div class="panel">
          <div class="panel-header"><h3>今日动态</h3></div>
          <div class="today-stats">
            <div class="today-item" v-for="d in todayStats" :key="d.label">
              <div class="today-icon" :style="{ background: d.bg }">
                <el-icon :size="16"><component :is="d.icon" /></el-icon>
              </div>
              <div class="today-info">
                <div class="today-value">{{ d.value }}</div>
                <div class="today-label">{{ d.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 流程详情弹窗 -->
    <el-dialog v-model="detailVisible" title="办件流程详情" width="650px">
      <template v-if="currentProcess">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="办件编号">{{ currentProcess.caseNo }}</el-descriptions-item>
          <el-descriptions-item label="事项名称">{{ currentProcess.itemName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentProcess.applicant }}</el-descriptions-item>
          <el-descriptions-item label="当前环节">{{ currentProcess.currentNode }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentProcess.submitTime }}</el-descriptions-item>
          <el-descriptions-item label="剩余时间">{{ currentProcess.remainText }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-timeline">
          <h4>流转记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(log, i) in currentProcess.logs"
              :key="i"
              :type="log.type"
              :timestamp="log.time"
              placement="top"
            >
              <div class="log-content">
                <div class="log-title">{{ log.node }}</div>
                <div class="log-user">{{ log.user }} · {{ log.dept }}</div>
                <div class="log-action">{{ log.action }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="warning" v-if="currentProcess?.timeout">催办</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { WarningFilled, CircleCheck, Clock, Promotion } from '@element-plus/icons-vue'

const listFilter = ref('all')

const metrics = [
  { label: '在办件数', value: 48, color: '#409EFF', trend: 12 },
  { label: '今日新收', value: 15, color: '#67C23A', trend: 8 },
  { label: '今日办结', value: 12, color: '#E6A23C', trend: -5 },
  { label: '超时件数', value: 3, color: '#F56C6C', trend: -20 },
  { label: '平均耗时', value: '2.3天', color: '#909399', trend: -10 }
]

const processes = ref([
  { caseNo: 'BJ20240117001', itemName: '营业执照申请', applicant: '张三', currentNode: '科室审核', urgent: true, timeout: false, submitTime: '2024-01-15 09:30', remainText: '剩余 6 小时', stepActive: 2, nodes: ['受理', '初审', '审核', '审批', '办结'], logs: [{ time: '2024-01-15 09:30', node: '提交申请', user: '张三', dept: '群众自助', action: '在线提交申请材料', type: 'primary' }, { time: '2024-01-15 10:00', node: '窗口受理', user: '陈受理', dept: '综合窗口', action: '材料收取，形式审查通过', type: 'success' }, { time: '2024-01-16 09:00', node: '科室初审', user: '李初审', dept: '审批一科', action: '材料核验通过，转审核', type: 'success' }, { time: '2024-01-17 08:30', node: '科室审核', user: '王审核', dept: '审批一科', action: '审核中...', type: 'primary' }] },
  { caseNo: 'BJ20240117002', itemName: '食品经营许可', applicant: '李四', currentNode: '现场核查', urgent: false, timeout: true, submitTime: '2024-01-10 14:00', remainText: '已超时 2 天', stepActive: 2, nodes: ['受理', '初审', '核查', '审批', '办结'], logs: [{ time: '2024-01-10 14:00', node: '提交申请', user: '李四', dept: '群众自助', action: '在线提交', type: 'primary' }, { time: '2024-01-10 15:30', node: '窗口受理', user: '陈受理', dept: '综合窗口', action: '受理通过', type: 'success' }, { time: '2024-01-11 09:00', node: '科室初审', user: '赵初审', dept: '审批二科', action: '初审通过，安排现场核查', type: 'success' }, { time: '2024-01-12 09:00', node: '现场核查', user: '待安排', dept: '-', action: '等待现场核查', type: 'warning' }] },
  { caseNo: 'BJ20240117003', itemName: '不动产登记', applicant: '王五', currentNode: '登簿发证', urgent: false, timeout: false, submitTime: '2024-01-12 10:00', remainText: '剩余 3 天', stepActive: 4, nodes: ['受理', '审核', '登簿', '发证', '办结'], logs: [{ time: '2024-01-12 10:00', node: '提交申请', user: '王五', dept: '窗口', action: '提交登记材料', type: 'primary' }, { time: '2024-01-12 14:00', node: '材料审核', user: '刘审核', dept: '不动产中心', action: '产权审核通过', type: 'success' }, { time: '2024-01-15 09:00', node: '登簿', user: '系统', dept: '不动产中心', action: '已登簿，等待发证', type: 'success' }] },
  { caseNo: 'BJ20240117004', itemName: '居民身份证换领', applicant: '赵六', currentNode: '制证中', urgent: false, timeout: false, submitTime: '2024-01-14 11:00', remainText: '剩余 5 天', stepActive: 3, nodes: ['受理', '审核', '制证', '发放', '办结'], logs: [{ time: '2024-01-14 11:00', node: '提交申请', user: '赵六', dept: '窗口', action: '提交换领申请', type: 'primary' }, { time: '2024-01-14 14:00', node: '信息审核', user: '孙审核', dept: '户籍科', action: '审核通过', type: 'success' }, { time: '2024-01-16 09:00', node: '制证', user: '系统', dept: '制证中心', action: '证件制作中', type: 'primary' }] },
  { caseNo: 'BJ20240117005', itemName: '公积金提取', applicant: '钱七', currentNode: '领导审批', urgent: true, timeout: false, submitTime: '2024-01-16 09:00', remainText: '剩余 4 小时', stepActive: 3, nodes: ['受理', '初审', '复审', '审批', '办结'], logs: [{ time: '2024-01-16 09:00', node: '提交申请', user: '钱七', dept: '窗口', action: '提交提取申请', type: 'primary' }, { time: '2024-01-16 10:00', node: '初审', user: '周初审', dept: '公积金中心', action: '材料核验通过', type: 'success' }, { time: '2024-01-16 14:00', node: '复审', user: '吴复审', dept: '公积金中心', action: '复审通过，转领导审批', type: 'success' }, { time: '2024-01-17 09:00', node: '领导审批', user: '郑主任', dept: '公积金中心', action: '审批中...', type: 'primary' }] },
  { caseNo: 'BJ20240117006', itemName: '建设工程规划许可', applicant: '海南建设集团', currentNode: '专家评审', urgent: false, timeout: true, submitTime: '2024-01-08 09:00', remainText: '已超时 5 天', stepActive: 2, nodes: ['受理', '初审', '评审', '审批', '办结'], logs: [{ time: '2024-01-08 09:00', node: '提交申请', user: '海南建设集团', dept: '企业端', action: '提交规划许可申请', type: 'primary' }, { time: '2024-01-08 14:00', node: '窗口受理', user: '陈受理', dept: '综合窗口', action: '受理通过', type: 'success' }, { time: '2024-01-09 09:00', node: '初审', user: '冯初审', dept: '规划局', action: '初审通过，安排专家评审', type: 'success' }, { time: '2024-01-10 09:00', node: '专家评审', user: '评审组', dept: '规划局', action: '等待专家评审', type: 'warning' }] }
])

const filteredProcesses = computed(() => {
  if (listFilter.value === 'timeout') return processes.value.filter(p => p.timeout)
  if (listFilter.value === 'urgent') return processes.value.filter(p => p.urgent)
  return processes.value
})

const deptRanking = [
  { name: '审批一科', count: 156, percent: 100 },
  { name: '审批二科', count: 132, percent: 85 },
  { name: '不动产中心', count: 98, percent: 63 },
  { name: '户籍科', count: 87, percent: 56 },
  { name: '公积金中心', count: 76, percent: 49 },
  { name: '社保中心', count: 65, percent: 42 }
]

const timeoutStats = [
  { label: '已超时', value: 3, color: '#F56C6C' },
  { label: '即将超时（2小时内）', value: 5, color: '#E6A23C' },
  { label: '正常', value: 40, color: '#67C23A' }
]

const todayStats = [
  { label: '新收', value: 15, icon: 'Promotion', bg: '#EFF6FF' },
  { label: '办结', value: 12, icon: 'CircleCheck', bg: '#F0FDF4' },
  { label: '超时', value: 3, icon: 'Clock', bg: '#FEF2F2' }
]

// 流程详情弹窗
const detailVisible = ref(false)
const currentProcess = ref(null)
const showProcessDetail = (item) => {
  currentProcess.value = item
  detailVisible.value = true
}
</script>

<style lang="scss" scoped>
.metric-cards {
  display: flex; gap: 16px; margin-bottom: 20px;
  .metric-card {
    flex: 1; background: #fff; border-radius: 10px; padding: 20px;
    text-align: center; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
    .metric-value { font-size: 28px; font-weight: 700; }
    .metric-label { font-size: 13px; color: #909399; margin-top: 4px; }
    .metric-trend { font-size: 12px; margin-top: 6px; &.up { color: #F56C6C; } &.down { color: #67C23A; } }
  }
}

.monitor-grid {
  display: flex; gap: 20px;
  .monitor-left { flex: 1; min-width: 0; }
  .monitor-right { width: 360px; flex-shrink: 0; display: flex; flex-direction: column; gap: 20px; }
}

.panel {
  background: #fff; border-radius: 10px; padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  .panel-header {
    display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;
    h3 { font-size: 15px; font-weight: 600; color: #1D2129; }
  }
}

.process-list { display: flex; flex-direction: column; gap: 12px; max-height: 600px; overflow-y: auto; }
.process-item {
  border: 1px solid #E5E7EB; border-radius: 8px; padding: 14px 16px;
  cursor: pointer; transition: all 0.2s;
  &:hover { border-color: #409EFF; box-shadow: 0 2px 8px rgba(64,158,255,0.12); }
  &.timeout { border-left: 3px solid #F56C6C; }
  &.urgent { border-left: 3px solid #E6A23C; }
  .process-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
  .process-no { font-size: 12px; color: #909399; }
  .process-name { font-size: 14px; font-weight: 500; color: #1D2129; margin-bottom: 6px; }
  .process-meta { font-size: 12px; color: #606266; display: flex; gap: 16px; margin-bottom: 10px; }
  .process-progress { margin-bottom: 10px; }
  .process-footer { display: flex; justify-content: space-between; font-size: 12px; color: #909399; }
  .process-remain { &.danger { color: #F56C6C; font-weight: 500; } &.warning { color: #E6A23C; } }
}

.dept-list { display: flex; flex-direction: column; gap: 12px; }
.dept-item {
  display: flex; align-items: center; gap: 10px; font-size: 13px;
  .rank {
    width: 22px; height: 22px; border-radius: 50%; background: #F3F4F6;
    display: flex; align-items: center; justify-content: center;
    font-size: 12px; color: #606266; font-weight: 600;
    &.top { background: #FEF3C7; color: #D97706; }
  }
  .dept-name { width: 80px; color: #303133; }
  .dept-bar { flex: 1; height: 8px; background: #F3F4F6; border-radius: 4px; overflow: hidden; }
  .dept-bar-fill { height: 100%; background: linear-gradient(90deg, #409EFF, #67C23A); border-radius: 4px; transition: width 0.6s; }
  .dept-count { width: 40px; text-align: right; font-weight: 600; color: #1D2129; }
}

.timeout-stats { display: flex; flex-direction: column; gap: 10px; }
.timeout-item {
  display: flex; align-items: center; gap: 8px; font-size: 13px;
  .timeout-dot { width: 8px; height: 8px; border-radius: 50%; }
  .timeout-label { flex: 1; color: #606266; }
  .timeout-value { font-weight: 600; color: #1D2129; }
}
.timeout-alert {
  display: flex; align-items: center; gap: 6px; margin-top: 12px;
  padding: 10px 14px; background: #FEF2F2; border-radius: 6px;
  font-size: 13px; color: #DC2626;
}

.today-stats { display: flex; gap: 12px; }
.today-item {
  flex: 1; display: flex; align-items: center; gap: 10px;
  padding: 12px; background: #F9FAFB; border-radius: 8px;
  .today-icon { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #409EFF; }
  .today-value { font-size: 18px; font-weight: 700; color: #1D2129; }
  .today-label { font-size: 12px; color: #909399; }
}

.detail-timeline {
  margin-top: 20px;
  h4 { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 16px; }
  .log-content {
    .log-title { font-size: 14px; font-weight: 500; }
    .log-user { font-size: 12px; color: #909399; margin-top: 4px; }
    .log-action { font-size: 13px; color: #606266; margin-top: 6px; background: #F9FAFB; padding: 6px 10px; border-radius: 4px; }
  }
}
</style>
