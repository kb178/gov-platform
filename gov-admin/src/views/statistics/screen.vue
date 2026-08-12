<template>
  <div class="data-screen">
    <!-- 顶部 -->
    <div class="screen-header">
      <div class="header-left">
        <span class="header-icon">🏛️</span>
        <span class="header-subtitle">实时数据</span>
      </div>
      <h1 class="header-title">海口市政务数据大屏</h1>
      <div class="header-right">
        <span class="header-time">{{ currentTime }}</span>
        <span class="header-date">{{ currentDate }}</span>
      </div>
    </div>

    <!-- 顶部指标卡 -->
    <div class="top-metrics">
      <div v-for="m in topMetrics" :key="m.label" class="metric-card">
        <div class="metric-icon" :style="{ background: m.bg }">{{ m.icon }}</div>
        <div class="metric-info">
          <div class="metric-value">{{ m.value }}</div>
          <div class="metric-label">{{ m.label }}</div>
        </div>
        <div class="metric-trend" :class="m.up ? 'up' : 'down'">
          {{ m.up ? '↑' : '↓' }} {{ m.trend }}
        </div>
      </div>
    </div>

    <!-- 主体三栏 -->
    <div class="screen-body">
      <!-- 左栏 -->
      <div class="screen-col left">
        <div class="panel">
          <div class="panel-title"><span class="dot"></span>办件趋势</div>
          <div ref="trendRef" class="chart-area"></div>
        </div>
        <div class="panel">
          <div class="panel-title"><span class="dot"></span>事项分类统计</div>
          <div ref="categoryRef" class="chart-area"></div>
        </div>
      </div>

      <!-- 中栏 -->
      <div class="screen-col center">
        <!-- 中间核心数字 -->
        <div class="center-hero">
          <div class="hero-item">
            <div class="hero-num">{{ animTotal }}</div>
            <div class="hero-label">今日办件总量</div>
          </div>
          <div class="hero-divider"></div>
          <div class="hero-item">
            <div class="hero-num green">{{ animDone }}</div>
            <div class="hero-label">今日已办结</div>
          </div>
          <div class="hero-divider"></div>
          <div class="hero-item">
            <div class="hero-num orange">{{ animProcessing }}</div>
            <div class="hero-label">当前办理中</div>
          </div>
        </div>
        <!-- 地图区域（模拟海口区域分布） -->
        <div class="panel map-panel">
          <div class="panel-title"><span class="dot"></span>各区办件分布</div>
          <div ref="mapRef" class="chart-area map-area"></div>
        </div>
        <!-- 底部实时滚动 -->
        <div class="panel scroll-panel">
          <div class="panel-title"><span class="dot"></span>实时办件动态</div>
          <div class="scroll-wrapper">
            <div class="scroll-list" :style="{ transform: `translateY(${scrollOffset}px)` }">
              <div v-for="(item, i) in [...realtimeList, ...realtimeList]" :key="i" class="scroll-item">
                <span class="scroll-time">{{ item.time }}</span>
                <span class="scroll-user">{{ item.user }}</span>
                <span class="scroll-action">{{ item.action }}</span>
                <el-tag :type="item.statusType" size="small" effect="dark">{{ item.status }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右栏 -->
      <div class="screen-col right">
        <div class="panel">
          <div class="panel-title"><span class="dot"></span>部门办件排行</div>
          <div ref="deptRankRef" class="chart-area"></div>
        </div>
        <div class="panel">
          <div class="panel-title"><span class="dot"></span>审批时效分析</div>
          <div ref="timeRef" class="chart-area"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'

// ===== 时间 =====
const now = ref(new Date())
let timer = null
const currentTime = computed(() => {
  const d = now.value
  return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}`
})
const currentDate = computed(() => {
  const d = now.value
  const week = ['日','一','二','三','四','五','六']
  return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日 星期${week[d.getDay()]}`
})

// ===== 顶部指标 =====
const topMetrics = [
  { icon: '📋', label: '办件总量', value: '158,236', trend: '12.5%', up: true, bg: 'linear-gradient(135deg,#1E40AF,#3B82F6)' },
  { icon: '✅', label: '已办结', value: '152,486', trend: '8.3%', up: true, bg: 'linear-gradient(135deg,#059669,#34D399)' },
  { icon: '⏳', label: '办理中', value: '4,523', trend: '3.2%', up: false, bg: 'linear-gradient(135deg,#D97706,#FBBF24)' },
  { icon: '⏱️', label: '平均耗时', value: '2.3天', trend: '0.5天', up: false, bg: 'linear-gradient(135deg,#7C3AED,#A78BFA)' },
  { icon: '📊', label: '好评率', value: '98.6%', trend: '1.2%', up: true, bg: 'linear-gradient(135deg,#DC2626,#F87171)' }
]

// ===== 中间数字动画 =====
const animTotal = ref(0)
const animDone = ref(0)
const animProcessing = ref(0)
let animFrame = null

function animateNum(target, ref, duration = 2000) {
  const start = 0
  const startTime = Date.now()
  function step() {
    const p = Math.min((Date.now() - startTime) / duration, 1)
    const ease = 1 - Math.pow(1 - p, 3)
    ref.value = Math.floor(start + (target - start) * ease)
    if (p < 1) requestAnimationFrame(step)
  }
  step()
}

// ===== 实时滚动 =====
const realtimeList = [
  { time: '14:32:18', user: '张**', action: '提交了居民身份证办理申请', status: '已受理', statusType: 'success' },
  { time: '14:31:45', user: '李**', action: '完成了营业执照年审', status: '已办结', statusType: 'primary' },
  { time: '14:30:22', user: '王**', action: '提交了不动产登记材料', status: '审核中', statusType: 'warning' },
  { time: '14:29:56', user: '陈**', action: '领取了食品经营许可证', status: '已办结', statusType: 'primary' },
  { time: '14:28:33', user: '刘**', action: '申请了社保卡补办', status: '已受理', statusType: 'success' },
  { time: '14:27:10', user: '赵**', action: '完成了公积金提取审批', status: '已办结', statusType: 'primary' },
  { time: '14:25:48', user: '孙**', action: '提交了建设工程规划许可', status: '待审核', statusType: 'info' },
  { time: '14:24:15', user: '周**', action: '完成了医师执业注册', status: '已办结', statusType: 'primary' },
  { time: '14:22:50', user: '吴**', action: '申请了残疾人证办理', status: '已受理', statusType: 'success' },
  { time: '14:21:08', user: '郑**', action: '提交了机动车登记申请', status: '审核中', statusType: 'warning' }
]
const scrollOffset = ref(0)
let scrollTimer = null

// ===== 图表 =====
const trendRef = ref(null)
const categoryRef = ref(null)
const mapRef = ref(null)
const deptRankRef = ref(null)
const timeRef = ref(null)
let charts = []

const screenBlue = '#3B82F6'
const screenBlueDark = '#1E40AF'
const screenGreen = '#34D399'
const screenGreenDark = '#059669'
const screenYellow = '#FBBF24'
const screenRed = '#F87171'
const screenPurple = '#A78BFA'
const screenText = '#E2E8F0'
const screenTextDim = '#94A3B8'
const screenLine = 'rgba(148,163,184,0.15)'

function initCharts() {
  // 办件趋势
  const trend = echarts.init(trendRef.value)
  const days = Array.from({ length: 30 }, (_, i) => `${i + 1}日`)
  const vals1 = [320,280,350,410,380,290,310,420,390,360,340,380,410,350,300,330,400,370,350,310,280,360,420,390,340,310,370,400,350,380]
  const vals2 = vals1.map(v => Math.floor(v * (0.85 + Math.random() * 0.1)))
  trend.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.9)', borderColor: screenLine, textStyle: { color: screenText } },
    legend: { data: ['受理量','办结量'], textStyle: { color: screenTextDim }, top: 0, right: 0 },
    grid: { top: 30, bottom: 20, left: 10, right: 10, containLabel: true },
    xAxis: { type: 'category', data: days, axisLine: { lineStyle: { color: screenLine } }, axisLabel: { color: screenTextDim, fontSize: 10 }, axisTick: { show: false } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: screenLine } }, axisLabel: { color: screenTextDim, fontSize: 10 } },
    series: [
      { name: '受理量', type: 'line', smooth: true, data: vals1, lineStyle: { color: screenBlue, width: 2 }, areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(59,130,246,0.3)'},{offset:1,color:'rgba(59,130,246,0)'}]) }, itemStyle: { color: screenBlue }, symbol: 'none' },
      { name: '办结量', type: 'line', smooth: true, data: vals2, lineStyle: { color: screenGreen, width: 2 }, areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(52,211,153,0.2)'},{offset:1,color:'rgba(52,211,153,0)'}]) }, itemStyle: { color: screenGreen }, symbol: 'none' }
    ]
  })
  charts.push(trend)

  // 事项分类 - 南丁格尔玫瑰图
  const cat = echarts.init(categoryRef.value)
  cat.setOption({
    tooltip: { trigger: 'item', backgroundColor: 'rgba(15,23,42,0.9)', borderColor: screenLine, textStyle: { color: screenText } },
    series: [{
      type: 'pie', radius: ['20%','70%'], center: ['50%','55%'], roseType: 'area',
      label: { color: screenTextDim, fontSize: 11 },
      data: [
        { value: 4256, name: '户籍办理', itemStyle: { color: screenBlue } },
        { value: 3692, name: '工商登记', itemStyle: { color: screenGreen } },
        { value: 2845, name: '不动产', itemStyle: { color: screenYellow } },
        { value: 2178, name: '社会保障', itemStyle: { color: screenPurple } },
        { value: 1543, name: '公积金', itemStyle: { color: screenRed } },
        { value: 1322, name: '其他', itemStyle: { color: '#475569' } }
      ]
    }]
  })
  charts.push(cat)

  // 各区办件分布 - 柱状图模拟地图
  const map = echarts.init(mapRef.value)
  const districts = ['龙华区','美兰区','秀英区','琼山区','桂林洋开发区','高新区']
  const dVals = [4520,3860,3240,2980,1860,1540]
  map.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'rgba(15,23,42,0.9)', borderColor: screenLine, textStyle: { color: screenText } },
    grid: { top: 10, bottom: 20, left: 10, right: 20, containLabel: true },
    xAxis: { type: 'category', data: districts, axisLine: { lineStyle: { color: screenLine } }, axisLabel: { color: screenTextDim, fontSize: 11 }, axisTick: { show: false } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: screenLine } }, axisLabel: { color: screenTextDim, fontSize: 10 } },
    series: [{
      type: 'bar', barWidth: 28, data: dVals.map((v, i) => ({
        value: v,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0,0,0,1,[
            { offset: 0, color: i === 0 ? screenBlue : i < 3 ? screenGreen : screenYellow },
            { offset: 1, color: i === 0 ? screenBlueDark : i < 3 ? screenGreenDark : '#92400E' }
          ]),
          borderRadius: [4,4,0,0]
        }
      })),
      label: { show: true, position: 'top', color: screenText, fontSize: 12, fontWeight: 600 }
    }]
  })
  charts.push(map)

  // 部门办件排行 - 横向条形图
  const deptRank = echarts.init(deptRankRef.value)
  const depts = ['教育局','公积金中心','人社局','自然资源局','市场监管局','公安局']
  const deptVals = [966,1543,1678,2245,2892,3256]
  deptRank.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'rgba(15,23,42,0.9)', borderColor: screenLine, textStyle: { color: screenText } },
    grid: { top: 5, bottom: 5, left: 5, right: 40, containLabel: true },
    xAxis: { type: 'value', show: false },
    yAxis: { type: 'category', data: depts, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: screenTextDim, fontSize: 12 } },
    series: [{
      type: 'bar', barWidth: 16, data: deptVals.map((v, i) => ({
        value: v,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0,0,1,0,[
            { offset: 0, color: i >= 4 ? screenBlue : i >= 2 ? screenGreen : screenYellow },
            { offset: 1, color: i >= 4 ? screenBlueDark : i >= 2 ? screenGreenDark : '#92400E' }
          ]),
          borderRadius: [0,4,4,0]
        }
      })),
      label: { show: true, position: 'right', color: screenText, fontSize: 12, fontWeight: 600 }
    }]
  })
  charts.push(deptRank)

  // 审批时效 - 雷达图
  const time = echarts.init(timeRef.value)
  time.setOption({
    tooltip: { backgroundColor: 'rgba(15,23,42,0.9)', borderColor: screenLine, textStyle: { color: screenText } },
    radar: {
      indicator: [
        { name: '户籍', max: 100 }, { name: '工商', max: 100 }, { name: '不动产', max: 100 },
        { name: '社保', max: 100 }, { name: '公积金', max: 100 }, { name: '教育', max: 100 }
      ],
      shape: 'polygon',
      axisName: { color: screenTextDim, fontSize: 11 },
      splitArea: { areaStyle: { color: ['rgba(59,130,246,0.05)','rgba(59,130,246,0.1)'] } },
      splitLine: { lineStyle: { color: screenLine } },
      axisLine: { lineStyle: { color: screenLine } }
    },
    series: [{
      type: 'radar',
      data: [
        { value: [95,88,82,90,86,78], name: '按时办结率', areaStyle: { color: 'rgba(59,130,246,0.2)' }, lineStyle: { color: screenBlue }, itemStyle: { color: screenBlue } },
        { value: [88,92,78,85,90,82], name: '群众满意率', areaStyle: { color: 'rgba(52,211,153,0.15)' }, lineStyle: { color: screenGreen }, itemStyle: { color: screenGreen } }
      ]
    }],
    legend: { data: ['按时办结率','群众满意率'], bottom: 0, textStyle: { color: screenTextDim, fontSize: 11 } }
  })
  charts.push(time)
}

function handleResize() { charts.forEach(c => c.resize()) }

onMounted(() => {
  timer = setInterval(() => { now.value = new Date() }, 1000)
  animateNum(1568, animTotal, 2500)
  animateNum(1423, animDone, 2500)
  animateNum(145, animProcessing, 2000)
  initCharts()
  window.addEventListener('resize', handleResize)
  // 滚动动画
  const itemH = 42
  const totalH = realtimeList.length * itemH
  scrollTimer = setInterval(() => {
    scrollOffset.value -= 1
    if (Math.abs(scrollOffset.value) >= totalH) scrollOffset.value = 0
  }, 50)
})

onBeforeUnmount(() => {
  clearInterval(timer)
  clearInterval(scrollTimer)
  cancelAnimationFrame(animFrame)
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
})
</script>

<style lang="scss" scoped>
.data-screen {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 9999;
  background: linear-gradient(180deg, #0B1120 0%, #0F172A 40%, #1E293B 100%);
  color: #E2E8F0; overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  padding: 0 24px 16px;
  display: flex; flex-direction: column;
}

// ===== 顶部 =====
.screen-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 0; position: relative;
  &::after { content: ''; position: absolute; bottom: 0; left: 10%; right: 10%; height: 2px; background: linear-gradient(90deg, transparent, #3B82F6, transparent); }
}
.header-left { display: flex; align-items: center; gap: 8px; }
.header-icon { font-size: 20px; }
.header-subtitle { font-size: 13px; color: #94A3B8; }
.header-title {
  font-size: 28px; font-weight: 700; letter-spacing: 6px;
  background: linear-gradient(90deg, #93C5FD, #3B82F6, #93C5FD);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.header-right { display: flex; flex-direction: column; align-items: flex-end; }
.header-time { font-size: 28px; font-weight: 700; color: #3B82F6; font-variant-numeric: tabular-nums; }
.header-date { font-size: 12px; color: #94A3B8; margin-top: 2px; }

// ===== 指标卡 =====
.top-metrics {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px;
  margin: 12px 0;
}
.metric-card {
  background: rgba(30,41,59,0.6); border: 1px solid rgba(148,163,184,0.12); border-radius: 10px;
  padding: 14px 18px; display: flex; align-items: center; gap: 14px;
  backdrop-filter: blur(10px); position: relative; overflow: hidden;
  &::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 2px; background: linear-gradient(90deg, transparent, rgba(59,130,246,0.5), transparent); }
}
.metric-icon {
  width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0;
}
.metric-info { flex: 1; }
.metric-value { font-size: 22px; font-weight: 700; color: #F1F5F9; }
.metric-label { font-size: 12px; color: #94A3B8; margin-top: 2px; }
.metric-trend {
  font-size: 12px; font-weight: 600; padding: 3px 8px; border-radius: 4px;
  &.up { color: #34D399; background: rgba(52,211,153,0.1); }
  &.down { color: #F87171; background: rgba(248,113,113,0.1); }
}

// ===== 主体 =====
.screen-body {
  flex: 1; display: grid; grid-template-columns: 1fr 1.4fr 1fr; gap: 16px;
  min-height: 0;
}
.screen-col { display: flex; flex-direction: column; gap: 16px; min-height: 0; }

// ===== 面板 =====
.panel {
  background: rgba(30,41,59,0.5); border: 1px solid rgba(148,163,184,0.1); border-radius: 10px;
  padding: 16px; flex: 1; display: flex; flex-direction: column; min-height: 0;
}
.panel-title {
  font-size: 14px; font-weight: 600; color: #E2E8F0; margin-bottom: 12px;
  display: flex; align-items: center; gap: 8px;
  .dot { width: 8px; height: 8px; border-radius: 50%; background: #3B82F6; display: inline-block; }
}
.chart-area { flex: 1; min-height: 0; }

// ===== 中栏特殊 =====
.center-hero {
  background: rgba(30,41,59,0.5); border: 1px solid rgba(148,163,184,0.1); border-radius: 10px;
  padding: 20px 24px; display: flex; align-items: center; justify-content: center; gap: 40px;
}
.hero-item { text-align: center; }
.hero-num {
  font-size: 42px; font-weight: 800; color: #3B82F6; font-variant-numeric: tabular-nums;
  &.green { color: #34D399; }
  &.orange { color: #FBBF24; }
}
.hero-label { font-size: 13px; color: #94A3B8; margin-top: 4px; }
.hero-divider { width: 1px; height: 50px; background: rgba(148,163,184,0.2); }

.map-panel { flex: 1.2; }
.map-area { min-height: 200px; }

// ===== 滚动列表 =====
.scroll-panel { flex: 0.8; }
.scroll-wrapper { flex: 1; overflow: hidden; position: relative; min-height: 0; }
.scroll-list { transition: none; }
.scroll-item {
  display: flex; align-items: center; gap: 10px; padding: 8px 0;
  border-bottom: 1px solid rgba(148,163,184,0.08); font-size: 13px;
}
.scroll-time { color: #64748B; font-variant-numeric: tabular-nums; width: 64px; flex-shrink: 0; }
.scroll-user { color: #94A3B8; width: 40px; flex-shrink: 0; }
.scroll-action { flex: 1; color: #CBD5E1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
