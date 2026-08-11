<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">办件统计</h1>
      <div class="header-right">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-button type="primary" @click="ElMessage.info('查询')">查询</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-header">
          <div class="stat-icon" :class="card.color">{{ card.icon }}</div>
          <el-tag :type="card.trendUp ? 'success' : 'danger'" size="small" effect="plain">
            {{ card.trendUp ? '↑' : '↓' }} {{ card.trend }}
          </el-tag>
        </div>
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-detail">
          <div v-for="d in card.details" :key="d.label" class="detail-item">
            <div class="detail-value">{{ d.value }}</div>
            <div class="detail-label">{{ d.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-header">
          <h3>📊 办件趋势</h3>
          <div class="chart-tabs">
            <span v-for="t in ['按日','按周','按月']" :key="t" :class="['tab',{ active: t === '按日' }]">{{ t }}</span>
          </div>
        </div>
        <div ref="trendRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header"><h3>📊 事项分布</h3></div>
        <div ref="pieRef" class="chart-box"></div>
      </div>
    </div>

    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-header"><h3>📊 部门办件量</h3></div>
        <div ref="deptRef" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header"><h3>🏆 事项办件排行</h3></div>
        <div class="rank-list">
          <div v-for="(item, i) in rankList" :key="i" class="rank-item">
            <span :class="['rank-num', i < 3 ? `top${i+1}` : 'other']">{{ i+1 }}</span>
            <div class="rank-info">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-dept">{{ item.dept }}</div>
            </div>
            <span class="rank-value">{{ item.value.toLocaleString() }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 部门明细表格 -->
    <div class="chart-card">
      <div class="chart-header">
        <h3>📋 部门办件明细</h3>
        <el-button size="small" @click="ElMessage.info('导出报表')">📥 导出报表</el-button>
      </div>
      <el-table :data="deptTable">
        <el-table-column prop="dept" label="部门名称" min-width="140">
          <template #default="{ row }"><span style="font-weight:500">{{ row.dept }}</span></template>
        </el-table-column>
        <el-table-column prop="total" label="办件总量" width="100" />
        <el-table-column prop="done" label="已办结" width="100" />
        <el-table-column prop="processing" label="办理中" width="100" />
        <el-table-column prop="rejected" label="已驳回" width="100" />
        <el-table-column label="按时办结率" width="120">
          <template #default="{ row }">
            <el-tag :type="parseFloat(row.rate) >= 96 ? 'success' : 'warning'" size="small">{{ row.rate }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="avgTime" label="平均时长" width="100" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const dateRange = ref([])

const statCards = [
  { icon: '📋', color: 'blue', value: '12,580', label: '办件总量', trend: '12.5%', trendUp: true, details: [{ value: '1,258', label: '本月' }, { value: '156', label: '今日' }] },
  { icon: '✅', color: 'green', value: '96.8%', label: '按时办结率', trend: '3.2%', trendUp: true, details: [{ value: '11,856', label: '已办结' }, { value: '724', label: '办理中' }] },
  { icon: '⏱️', color: 'orange', value: '2.5天', label: '平均审批时长', trend: '0.5天', trendUp: false, details: [{ value: '1.8天', label: '最快' }, { value: '5.2天', label: '最慢' }] },
  { icon: '⚠️', color: 'red', value: '3.2%', label: '超时率', trend: '2.1%', trendUp: false, details: [{ value: '48', label: '超时件' }, { value: '12', label: '催办件' }] }
]

const rankList = [
  { name: '居民身份证办理', dept: '公安局', value: 2356 },
  { name: '营业执照办理', dept: '市场监管局', value: 1892 },
  { name: '不动产登记', dept: '自然资源局', value: 1245 },
  { name: '食品经营许可证', dept: '市场监管局', value: 856 },
  { name: '社保卡申领', dept: '人社局', value: 678 }
]

const deptTable = [
  { dept: '公安局', total: 3256, done: 3102, processing: 128, rejected: 26, rate: '98.2%', avgTime: '2.1天' },
  { dept: '市场监管局', total: 2892, done: 2756, processing: 102, rejected: 34, rate: '97.5%', avgTime: '2.3天' },
  { dept: '自然资源局', total: 2245, done: 2156, processing: 78, rejected: 11, rate: '96.8%', avgTime: '2.8天' },
  { dept: '人社局', total: 1678, done: 1598, processing: 65, rejected: 15, rate: '95.2%', avgTime: '2.5天' },
  { dept: '公积金中心', total: 1543, done: 1478, processing: 52, rejected: 13, rate: '96.5%', avgTime: '2.2天' },
  { dept: '教育局', total: 966, done: 912, processing: 45, rejected: 9, rate: '94.8%', avgTime: '3.1天' }
]

const trendRef = ref(null)
const pieRef = ref(null)
const deptRef = ref(null)
let charts = []

function initCharts() {
  const trend = echarts.init(trendRef.value)
  trend.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['1月1日','1月2日','1月3日','1月4日','1月5日','1月6日','1月7日','1月8日','1月9日','1月10日','1月11日','1月12日','1月13日','1月14日','1月15日','1月16日'] },
    yAxis: { type: 'value' },
    series: [{ name: '办件量', type: 'bar', barWidth: 20, data: [120,132,101,134,90,230,210,182,192,201,154,190,230,210,182,156], itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#3B82F6' }, { offset: 1, color: '#1E40AF' }] }, borderRadius: [4,4,0,0] } }]
  })
  charts.push(trend)

  const pie = echarts.init(pieRef.value)
  pie.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{ name: '事项分布', type: 'pie', radius: ['40%','70%'], label: { show: false }, emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } }, data: [
      { value: 3256, name: '户籍办理', itemStyle: { color: '#1E40AF' } },
      { value: 2892, name: '工商登记', itemStyle: { color: '#3B82F6' } },
      { value: 2245, name: '不动产', itemStyle: { color: '#60A5FA' } },
      { value: 1678, name: '社会保障', itemStyle: { color: '#93C5FD' } },
      { value: 1543, name: '公积金', itemStyle: { color: '#BFDBFE' } },
      { value: 966, name: '其他', itemStyle: { color: '#DBEAFE' } }
    ] }]
  })
  charts.push(pie)

  const dept = echarts.init(deptRef.value)
  dept.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: ['教育局','公积金中心','人社局','自然资源局','市场监管局','公安局'] },
    series: [
      { name: '已办结', type: 'bar', stack: 'total', data: [912,1478,1598,2156,2756,3102], itemStyle: { color: '#1E40AF' } },
      { name: '办理中', type: 'bar', stack: 'total', data: [45,52,65,78,102,128], itemStyle: { color: '#60A5FA' } },
      { name: '已驳回', type: 'bar', stack: 'total', data: [9,13,15,11,34,26], itemStyle: { color: '#EF4444' } }
    ]
  })
  charts.push(dept)
}

function handleResize() { charts.forEach(c => c.resize()) }

onMounted(() => { initCharts(); window.addEventListener('resize', handleResize) })
onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); charts.forEach(c => c.dispose()) })
</script>

<style lang="scss" scoped>
.page-header { display: flex; align-items: center; justify-content: space-between; .header-right { display: flex; gap: 12px; align-items: center; } }

.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
.stat-card {
  background: white; border-radius: 12px; padding: 24px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); position: relative; overflow: hidden;
  &::after { content: ''; position: absolute; top: 0; right: 0; width: 100px; height: 100px; background: linear-gradient(135deg, transparent 50%, rgba(30,64,175,0.05) 50%); border-radius: 0 0 0 100px; }
}
.stat-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.stat-icon {
  width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px;
  &.blue { background: #EFF6FF; } &.green { background: #F0FDF4; } &.orange { background: #FFF7ED; } &.red { background: #FEF2F2; }
}
.stat-value { font-size: 32px; font-weight: 700; color: #1F2937; margin-bottom: 4px; }
.stat-label { font-size: 14px; color: #6B7280; }
.stat-detail {
  margin-top: 16px; padding-top: 16px; border-top: 1px solid #F3F4F6; display: flex; justify-content: space-between;
  .detail-item { text-align: center; } .detail-value { font-size: 16px; font-weight: 600; color: #1F2937; } .detail-label { font-size: 12px; color: #9CA3AF; margin-top: 4px; }
}

.charts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px; }
.chart-card { background: white; border-radius: 12px; padding: 24px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.chart-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; h3 { font-size: 16px; font-weight: 600; color: #1F2937; } }
.chart-tabs { display: flex; gap: 8px; .tab { padding: 4px 12px; background: #F3F4F6; border-radius: 4px; font-size: 12px; color: #6B7280; cursor: pointer; &.active { background: #EFF6FF; color: #1E40AF; } } }
.chart-box { height: 300px; }

.rank-list { display: flex; flex-direction: column; gap: 12px; }
.rank-item { display: flex; align-items: center; padding: 12px; background: #F9FAFB; border-radius: 8px; }
.rank-num {
  width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; margin-right: 12px; flex-shrink: 0;
  &.top1 { background: #FEF3C7; color: #D97706; } &.top2 { background: #E5E7EB; color: #6B7280; } &.top3 { background: #FED7AA; color: #EA580C; } &.other { background: #F3F4F6; color: #9CA3AF; }
}
.rank-info { flex: 1; } .rank-name { font-size: 14px; font-weight: 500; color: #1F2937; } .rank-dept { font-size: 12px; color: #9CA3AF; margin-top: 2px; }
.rank-value { font-size: 16px; font-weight: 600; color: #1E40AF; }
</style>
