<template>
  <div class="home-page">
    <!-- 搜索区域 -->
    <section class="search-section">
      <div class="search-container">
        <div class="search-title">
          <h2>海口市政务服务平台</h2>
          <p>让数据多跑路，让群众少跑腿</p>
        </div>
        <div class="search-box">
          <div class="search-input-wrapper">
            <span class="search-icon">🔍</span>
            <input
              v-model="searchKeyword"
              type="text"
              class="search-input"
              placeholder="搜索您需要办理的事项，如：身份证办理、营业执照..."
              @keyup.enter="handleSearch"
              @focus="showHotSearch = true"
              @blur="hideHotSearch"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>

            <!-- 热门搜索 -->
            <div v-show="showHotSearch" class="hot-search-panel">
              <div class="hot-search-title">🔥 热门搜索</div>
              <div class="hot-search-tags">
                <el-tag
                  v-for="tag in hotSearchTags"
                  :key="tag"
                  class="hot-tag"
                  @mousedown.prevent="handleHotSearch(tag)"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-section">
      <div class="container">
        <div class="quick-grid">
          <div
            v-for="(item, index) in quickEntries"
            :key="item.id"
            class="quick-item"
            :style="{ animationDelay: `${index * 0.1}s` }"
            @click="handleQuickEntry(item)"
          >
            <div :class="['quick-icon', item.color]">
              <span class="icon-emoji">{{ item.icon }}</span>
            </div>
            <div class="quick-name">{{ item.name }}</div>
            <div v-if="item.badge" class="quick-badge">{{ item.badge }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据统计 -->
    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div v-for="(stat, index) in statsData" :key="stat.label" class="stat-card">
            <div class="stat-number">
              <span ref="statNumbers" :data-target="stat.target">{{ animatedStats[index] }}</span>
              <span class="stat-suffix">{{ stat.suffix }}</span>
            </div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门事项 -->
    <section class="content-section">
      <div class="container">
        <div class="section-header">
          <h3 class="section-title">
            <span class="icon">🔥</span>
            热门事项
          </h3>
          <router-link to="/items" class="section-more">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>

        <div class="hot-grid">
          <div
            v-for="(item, index) in hotItems"
            :key="item.id"
            class="hot-card"
            :style="{ animationDelay: `${index * 0.15}s` }"
            @click="handleItemClick(item)"
          >
            <div v-if="item.tag" :class="['hot-tag-badge', item.tagType]">
              {{ item.tag }}
            </div>
            <div class="hot-card-header">
              <div class="hot-card-icon">{{ item.icon }}</div>
              <div class="hot-card-info">
                <h4>{{ item.name }}</h4>
                <p>{{ item.dept }}</p>
              </div>
            </div>
            <div class="hot-card-desc">{{ item.desc }}</div>
            <div class="hot-card-footer">
              <span class="hot-card-tag">{{ item.category }}</span>
              <span class="hot-card-count">本月办理 {{ item.count }} 件</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 办件进度和快捷操作 -->
    <section class="content-section progress-section-wrapper">
      <div class="container">
        <div class="progress-grid">
          <!-- 我的办件 -->
          <div class="progress-card">
            <h4 class="card-title"><span>📋</span> 我的办件</h4>

            <template v-if="userStore.isLoggedIn">
              <div class="progress-list">
                <div v-for="item in myApplications" :key="item.id" class="progress-item">
                  <div :class="['progress-icon', item.status]">
                    {{ item.statusIcon }}
                  </div>
                  <div class="progress-info">
                    <div class="progress-name">{{ item.name }}</div>
                    <div :class="['progress-status', item.status]">
                      {{ item.statusText }}
                    </div>
                  </div>
                  <div class="progress-date">{{ item.date }}</div>
                </div>
              </div>
              <el-button text type="primary" class="view-all-btn">
                查看全部办件 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </template>

            <template v-else>
              <div class="empty-state">
                <div class="empty-icon">🔐</div>
                <div class="empty-text">登录后查看办件进度</div>
                <div class="empty-desc">登录即可查看您的办件进度、证照信息等</div>
                <el-button type="primary" @click="$router.push('/login')"> 立即登录 </el-button>
              </div>
            </template>
          </div>

          <!-- 右侧：快捷操作 + 公告 -->
          <div class="right-panel">
            <!-- 快捷操作 -->
            <div class="action-card">
              <h4 class="card-title"><span>⚡</span> 快捷操作</h4>
              <div class="action-grid">
                <div
                  v-for="action in quickActions"
                  :key="action.id"
                  class="action-item"
                  @click="handleAction(action)"
                >
                  <span class="action-icon">{{ action.icon }}</span>
                  <span class="action-text">{{ action.name }}</span>
                </div>
              </div>
            </div>

            <!-- 最新公告 -->
            <div class="notice-card">
              <h4 class="card-title">
                <span>📢</span> 最新公告
                <el-button text class="notice-more">更多</el-button>
              </h4>
              <div class="notice-list">
                <div v-for="notice in notices" :key="notice.id" class="notice-item">
                  <el-tag
                    :type="notice.type === 'important' ? 'danger' : 'info'"
                    size="small"
                    class="notice-tag"
                  >
                    {{ notice.type === 'important' ? '重要' : '通知' }}
                  </el-tag>
                  <span class="notice-title">{{ notice.title }}</span>
                  <span class="notice-date">{{ notice.date }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部链接 -->
    <section class="bottom-links-section">
      <div class="container">
        <div class="bottom-links">
          <div class="links-grid">
            <div v-for="group in bottomLinks" :key="group.title" class="link-group">
              <h5>{{ group.title }}</h5>
              <a
                v-for="link in group.links"
                :key="link.name"
                href="#"
                @click.prevent="handleLink(link)"
              >
                {{ link.name }}
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 回到顶部 -->
    <transition name="fade">
      <div v-show="showBackTop" class="back-top" @click="scrollToTop">
        <el-icon :size="20"><Top /></el-icon>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Top } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// ========== 搜索功能 ==========
const searchKeyword = ref('')
const showHotSearch = ref(false)

const hotSearchTags = ref([
  '身份证办理',
  '营业执照',
  '社保查询',
  '公积金提取',
  '不动产登记',
  '居住证'
])

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  showHotSearch.value = false
  router.push({ path: '/items', query: { keyword: searchKeyword.value } })
}

const handleHotSearch = tag => {
  searchKeyword.value = tag
  handleSearch()
}

const hideHotSearch = () => {
  setTimeout(() => {
    showHotSearch.value = false
  }, 200)
}

// ========== 快捷入口 ==========
const quickEntries = ref([
  { id: 1, name: '户籍办理', icon: '🏠', color: 'blue', category: 'household', badge: '热门' },
  { id: 2, name: '社会保障', icon: '🏥', color: 'green', category: 'social' },
  { id: 3, name: '工商登记', icon: '💼', color: 'orange', category: 'business' },
  { id: 4, name: '公积金', icon: '🏦', color: 'purple', category: 'fund' },
  { id: 5, name: '教育服务', icon: '🎓', color: 'red', category: 'education' }
])

const handleQuickEntry = item => {
  router.push({ path: '/items', query: { category: item.category } })
}

// ========== 数据统计（带滚动动画） ==========
const statsData = ref([
  { value: 1258, suffix: '', label: '可办事项', target: 1258 },
  { value: 98.5, suffix: '%', label: '好评率', target: 98.5 },
  { value: 3.2, suffix: '天', label: '平均办结时间', target: 3.2 },
  { value: 156, suffix: '万', label: '累计服务人次', target: 156 }
])

const animatedStats = ref([0, 0, 0, 0])
let statsAnimated = false

const animateNumbers = () => {
  if (statsAnimated) return
  statsAnimated = true

  statsData.value.forEach((stat, index) => {
    const target = stat.target
    const duration = 2000
    const startTime = Date.now()
    const isFloat = target % 1 !== 0

    const animate = () => {
      const elapsed = Date.now() - startTime
      const progress = Math.min(elapsed / duration, 1)
      const easeProgress = 1 - Math.pow(1 - progress, 3) // 缓动函数
      const current = target * easeProgress

      animatedStats.value[index] = isFloat ? current.toFixed(1) : Math.floor(current)

      if (progress < 1) {
        requestAnimationFrame(animate)
      } else {
        animatedStats.value[index] = isFloat ? target.toFixed(1) : target
      }
    }

    setTimeout(() => {
      requestAnimationFrame(animate)
    }, index * 200)
  })
}

// ========== 热门事项 ==========
const hotItems = ref([
  {
    id: 1,
    name: '居民身份证办理',
    dept: '公安局',
    icon: '📄',
    desc: '本市户籍居民首次申领、换领、补领居民身份证，支持网上预约、线下办理。',
    category: '户籍办理',
    count: '2,356',
    tag: '热门',
    tagType: 'hot'
  },
  {
    id: 2,
    name: '营业执照办理',
    dept: '市场监督管理局',
    icon: '🏢',
    desc: '企业设立、变更、注销登记，全程网上办理，最快1个工作日出证。',
    category: '工商登记',
    count: '1,892',
    tag: '新增',
    tagType: 'new'
  },
  {
    id: 3,
    name: '不动产登记',
    dept: '自然资源和规划局',
    icon: '🏠',
    desc: '不动产首次登记、转移登记、变更登记、注销登记等业务办理。',
    category: '不动产',
    count: '1,245'
  }
])

const handleItemClick = item => {
  router.push(`/items/${item.id}`)
}

// ========== 我的办件 ==========
const myApplications = ref([
  {
    id: 1,
    name: '居民身份证换领',
    status: 'pending',
    statusIcon: '⏳',
    statusText: '审核中',
    date: '2024-01-15'
  },
  {
    id: 2,
    name: '营业执照变更',
    status: 'approved',
    statusIcon: '✅',
    statusText: '已办结',
    date: '2024-01-10'
  },
  {
    id: 3,
    name: '食品经营许可证',
    status: 'rejected',
    statusIcon: '❌',
    statusText: '已驳回',
    date: '2024-01-08'
  }
])

// ========== 快捷操作 ==========
const quickActions = ref([
  { id: 1, name: '在线申请', icon: '📝', path: '/apply' },
  { id: 2, name: '进度查询', icon: '🔍', path: '/progress' },
  { id: 3, name: '我的证照', icon: '📜', path: '/my-license' },
  { id: 4, name: '消息通知', icon: '🔔', path: '/message' },
  { id: 5, name: '实名认证', icon: '🆔', path: '/real-name' },
  { id: 6, name: '咨询投诉', icon: '📞', path: '#' }
])

const handleAction = action => {
  if (action.path === '#') {
    ElMessage.info('功能开发中')
    return
  }
  router.push(action.path)
}

// ========== 公告 ==========
const notices = ref([
  {
    id: 1,
    type: 'important',
    title: '关于2024年春节假期政务服务大厅放假通知',
    date: '01-15'
  },
  {
    id: 2,
    type: 'normal',
    title: '不动产登记业务办理流程优化公告',
    date: '01-12'
  },
  {
    id: 3,
    type: 'normal',
    title: '社保业务网上办理指南更新',
    date: '01-10'
  }
])

// ========== 底部链接 ==========
const bottomLinks = ref([
  {
    title: '户籍办理',
    links: [
      { name: '身份证办理', path: '/items' },
      { name: '户口迁移', path: '/items' },
      { name: '居住证办理', path: '/items' },
      { name: '出生登记', path: '/items' }
    ]
  },
  {
    title: '社会保障',
    links: [
      { name: '社保查询', path: '/items' },
      { name: '医保报销', path: '/items' },
      { name: '养老金领取', path: '/items' },
      { name: '失业登记', path: '/items' }
    ]
  },
  {
    title: '工商登记',
    links: [
      { name: '营业执照', path: '/items' },
      { name: '食品经营许可', path: '/items' },
      { name: '企业变更', path: '/items' },
      { name: '企业注销', path: '/items' }
    ]
  },
  {
    title: '其他服务',
    links: [
      { name: '公积金提取', path: '/items' },
      { name: '不动产登记', path: '/items' },
      { name: '婚姻登记', path: '/items' },
      { name: '交通违章', path: '/items' }
    ]
  }
])

const handleLink = link => {
  router.push(link.path)
}

// ========== 回到顶部 ==========
const showBackTop = ref(false)

const handleScroll = () => {
  showBackTop.value = window.scrollY > 500

  // 检查统计数字是否进入视口
  const statsSection = document.querySelector('.stats-section')
  if (statsSection) {
    const rect = statsSection.getBoundingClientRect()
    if (rect.top < window.innerHeight && rect.bottom > 0) {
      animateNumbers()
    }
  }
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// ========== 生命周期 ==========
onMounted(() => {
  window.addEventListener('scroll', handleScroll)

  // 初始检查统计数字
  nextTick(() => {
    handleScroll()
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.home-page {
  margin: -24px;
}

/* 搜索区域 */
.search-section {
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  padding: 48px 0 80px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 60%);
    animation: float 15s ease-in-out infinite;
  }
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(30px, -30px);
  }
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  z-index: 1;
}

.search-title {
  text-align: center;
  color: white;
  margin-bottom: 32px;

  h2 {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 12px;
  }

  p {
    font-size: 16px;
    opacity: 0.9;
  }
}

.search-box {
  position: relative;
}

.search-input-wrapper {
  display: flex;
  position: relative;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  overflow: hidden;

  .search-icon {
    position: absolute;
    left: 16px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 20px;
    z-index: 1;
  }

  .search-input {
    flex: 1;
    height: 56px;
    padding: 0 16px 0 48px;
    border: none;
    font-size: 16px;
    outline: none;
    background: white;

    &::placeholder {
      color: #9ca3af;
    }
  }

  .search-btn {
    width: 120px;
    height: 56px;
    background: #1e40af;
    color: white;
    border: none;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #1e3a8a;
    }
  }
}

/* 热门搜索面板 */
.hot-search-panel {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 16px;
  margin-top: -1px;
  z-index: 10;
}

.hot-search-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.hot-search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-tag {
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    transform: scale(1.05);
  }
}

/* 快捷入口 */
.quick-section {
  margin-top: -40px;
  position: relative;
  z-index: 10;
  padding-bottom: 32px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.quick-item {
  background: white;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  animation: fadeInUp 0.6s ease-out both;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

    .icon-emoji {
      transform: scale(1.2);
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.quick-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;

  &.blue {
    background: #eff6ff;
  }
  &.green {
    background: #d1fae5;
  }
  &.orange {
    background: #fef3c7;
  }
  &.purple {
    background: #e0e7ff;
  }
  &.red {
    background: #fee2e2;
  }
}

.icon-emoji {
  font-size: 28px;
  transition: transform 0.3s;
}

.quick-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.quick-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #ef4444;
  color: white;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
}

/* 数据统计 */
.stats-section {
  padding: 0 0 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 8px;
  font-variant-numeric: tabular-nums;
}

.stat-suffix {
  font-size: 20px;
  font-weight: 400;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 内容区块 */
.content-section {
  padding: 0 0 48px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;

  .icon {
    font-size: 24px;
  }
}

.section-more {
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  transition: all 0.2s;

  &:hover {
    color: var(--primary);
    transform: translateX(4px);
  }
}

/* 热门事项 */
.hot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.hot-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
  position: relative;
  animation: fadeInUp 0.6s ease-out both;

  &:hover {
    border-color: var(--primary-lighter);
    box-shadow: 0 4px 16px rgba(30, 64, 175, 0.1);
    transform: translateY(-4px);
  }
}

.hot-tag-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;

  &.hot {
    background: #fee2e2;
    color: #ef4444;
  }

  &.new {
    background: #d1fae5;
    color: #10b981;
  }
}

.hot-card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.hot-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: var(--primary-bg);
}

.hot-card-info {
  h4 {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  p {
    font-size: 12px;
    color: var(--text-secondary);
  }
}

.hot-card-desc {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hot-card-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  background: var(--primary-bg);
  color: var(--primary);
}

.hot-card-count {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 办件进度和快捷操作 */
.progress-section-wrapper {
  padding-bottom: 0;
}

.progress-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.progress-card,
.action-card,
.notice-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;

  span {
    font-size: 20px;
  }

  .notice-more {
    margin-left: auto;
    font-size: 13px;
  }
}

/* 办件列表 */
.progress-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    background: #f0f5ff;
  }
}

.progress-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-right: 16px;
  flex-shrink: 0;

  &.pending {
    background: var(--warning-bg);
  }
  &.approved {
    background: var(--success-bg);
  }
  &.rejected {
    background: var(--danger-bg);
  }
}

.progress-info {
  flex: 1;
  min-width: 0;
}

.progress-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-status {
  font-size: 12px;

  &.pending {
    color: var(--warning);
  }
  &.approved {
    color: var(--success);
  }
  &.rejected {
    color: var(--danger);
  }
}

.progress-date {
  font-size: 12px;
  color: var(--text-secondary);
  flex-shrink: 0;
  margin-left: 12px;
}

.view-all-btn {
  width: 100%;
  margin-top: 16px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;

  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 16px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 8px;
  }

  .empty-desc {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 20px;
  }
}

/* 右侧面板 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 快捷操作 */
.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--primary-bg);
    transform: translateX(4px);
  }
}

.action-icon {
  font-size: 24px;
}

.action-text {
  font-size: 14px;
  color: var(--text-primary);
}

/* 公告 */
.notice-list {
  display: flex;
  flex-direction: column;
}

.notice-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: all 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    padding-left: 8px;

    .notice-title {
      color: var(--primary);
    }
  }
}

.notice-tag {
  margin-right: 12px;
  flex-shrink: 0;
}

.notice-title {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.notice-date {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: 16px;
  flex-shrink: 0;
}

/* 底部链接 */
.bottom-links-section {
  padding: 0 0 48px;
}

.bottom-links {
  background: white;
  border-radius: 12px;
  padding: 32px;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

.link-group {
  h5 {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 16px;
  }

  a {
    display: block;
    font-size: 13px;
    color: var(--text-secondary);
    padding: 6px 0;
    text-decoration: none;
    transition: all 0.2s;

    &:hover {
      color: var(--primary);
      padding-left: 8px;
    }
  }
}

/* 回到顶部 */
.back-top {
  position: fixed;
  right: 40px;
  bottom: 40px;
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  transition: all 0.3s;
  z-index: 100;
  color: var(--text-secondary);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.16);
    color: var(--primary);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .quick-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .hot-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .progress-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .search-title h2 {
    font-size: 24px;
  }

  .quick-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .hot-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .links-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .back-top {
    right: 20px;
    bottom: 20px;
  }
}
</style>
