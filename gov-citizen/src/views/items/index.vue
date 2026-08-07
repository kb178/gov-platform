<template>
  <div class="items-page">
    <!-- 页面横幅 -->
    <section class="page-banner">
      <div class="container">
        <h2>事项办理</h2>
        <p>在线办理各类政务服务事项</p>
      </div>
    </section>

    <div class="page-container">
      <div class="container">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <div class="search-row">
            <div class="search-input-wrap">
              <span class="search-icon">🔍</span>
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索事项名称，如：身份证、营业执照..."
                @keyup.enter="handleSearch"
              >
            </div>
            <button class="search-btn" @click="handleSearch">搜 索</button>
          </div>
        </div>

        <!-- 主体布局 -->
        <div class="main-layout">
          <!-- 左侧分类 -->
          <aside class="sidebar">
            <div class="category-card">
              <div class="category-header">
                <span>📂</span> 事项分类
              </div>
              <div class="category-list">
                <div
                  v-for="cat in categories"
                  :key="cat.id"
                  :class="['category-item', { active: activeCategory === cat.id }]"
                  @click="handleCategoryChange(cat.id)"
                >
                  <span class="cat-icon">{{ cat.icon }}</span>
                  <span class="cat-name">{{ cat.name }}</span>
                  <span class="cat-count">{{ cat.count }}</span>
                </div>
              </div>
            </div>
          </aside>

          <!-- 右侧内容 -->
          <div class="content">
            <!-- 筛选条件 -->
            <div class="filter-bar">
              <span class="filter-label">主题：</span>
              <div class="filter-tags">
                <span
                  v-for="filter in filters"
                  :key="filter.id"
                  :class="['filter-tag', { active: activeFilter === filter.id }]"
                  @click="handleFilterChange(filter.id)"
                >
                  {{ filter.name }}
                </span>
              </div>
            </div>

            <!-- 结果统计和排序 -->
            <div class="result-bar">
              <div class="result-count">
                共找到 <span>{{ total }}</span> 个事项
              </div>
              <div class="sort-options">
                <span
                  v-for="sort in sortOptions"
                  :key="sort.id"
                  :class="['sort-item', { active: activeSort === sort.id }]"
                  @click="handleSortChange(sort.id)"
                >
                  {{ sort.name }}
                </span>
              </div>
            </div>

            <!-- 事项列表 -->
            <div class="item-list">
              <div
                v-for="item in itemList"
                :key="item.id"
                class="item-card"
                @click="handleItemClick(item)"
              >
                <div class="item-icon">{{ item.icon }}</div>
                <div class="item-info">
                  <div class="item-title">
                    {{ item.name }}
                    <span v-if="item.isHot" class="hot-badge">热门</span>
                  </div>
                  <div class="item-desc">{{ item.desc }}</div>
                  <div class="item-meta">
                    <span>🏛️ {{ item.dept }}</span>
                    <span>⏱️ {{ item.duration }}</span>
                    <span>📊 已办理 {{ item.count }} 件</span>
                  </div>
                </div>
                <div class="item-actions">
                  <span class="item-btn item-btn-primary" @click.stop="handleApply(item)">立即办理</span>
                  <span class="item-btn item-btn-outline" @click.stop="handleItemClick(item)">查看详情</span>
                </div>
              </div>

              <!-- 加载状态 -->
              <div v-if="loading" class="loading-state">
                <div class="loading-spinner"></div>
                <p>加载中...</p>
              </div>

              <!-- 空状态 -->
              <div v-if="!loading && itemList.length === 0" class="empty-state">
                <div class="empty-icon">📭</div>
                <div class="empty-title">暂无相关事项</div>
                <div class="empty-desc">您可以尝试更换搜索关键词或筛选条件</div>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="totalPages > 1" class="pagination-wrap">
              <div class="pagination">
                <span
                  :class="['page-btn', { disabled: currentPage === 1 }]"
                  @click="handlePageChange(currentPage - 1)"
                >‹</span>
                <span
                  v-for="page in displayPages"
                  :key="page"
                  :class="['page-btn', { active: currentPage === page, ellipsis: page === '...' }]"
                  @click="page !== '...' && handlePageChange(page)"
                >{{ page }}</span>
                <span
                  :class="['page-btn', { disabled: currentPage === totalPages }]"
                  @click="handlePageChange(currentPage + 1)"
                >›</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// ========== 搜索 ==========
const searchKeyword = ref('')

const handleSearch = () => {
  currentPage.value = 1
  fetchItemList()
}

// ========== 分类 ==========
const activeCategory = ref('all')

const categories = ref([
  { id: 'all', name: '全部事项', icon: '📋', count: 1258 },
  { id: 'household', name: '户籍办理', icon: '🏠', count: 186 },
  { id: 'social', name: '社会保障', icon: '🏥', count: 245 },
  { id: 'business', name: '工商登记', icon: '💼', count: 198 },
  { id: 'fund', name: '公积金', icon: '🏦', count: 67 },
  { id: 'education', name: '教育服务', icon: '🎓', count: 124 },
  { id: 'traffic', name: '交通服务', icon: '🚗', count: 89 },
  { id: 'estate', name: '不动产', icon: '🏠', count: 76 },
  { id: 'marriage', name: '婚姻登记', icon: '💑', count: 32 },
  { id: 'birth', name: '生育服务', icon: '👶', count: 45 },
  { id: 'elderly', name: '养老助老', icon: '👴', count: 58 },
  { id: 'other', name: '其他服务', icon: '📋', count: 138 }
])

const handleCategoryChange = (id) => {
  activeCategory.value = id
  currentPage.value = 1
  fetchItemList()
}

// ========== 筛选 ==========
const activeFilter = ref('all')

const filters = ref([
  { id: 'all', name: '全部' },
  { id: 'personal', name: '个人办事' },
  { id: 'enterprise', name: '企业办事' },
  { id: 'org', name: '社会组织' }
])

const handleFilterChange = (id) => {
  activeFilter.value = id
  currentPage.value = 1
  fetchItemList()
}

// ========== 排序 ==========
const activeSort = ref('default')

const sortOptions = ref([
  { id: 'default', name: '默认排序' },
  { id: 'hot', name: '热度优先' },
  { id: 'new', name: '最新发布' }
])

const handleSortChange = (id) => {
  activeSort.value = id
  fetchItemList()
}

// ========== 列表数据 ==========
const loading = ref(false)
const itemList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(6)

// 分页显示逻辑
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const displayPages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    if (current > 3) pages.push('...')
    for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
      pages.push(i)
    }
    if (current < total - 2) pages.push('...')
    pages.push(total)
  }

  return pages
})

// 模拟数据
const mockItems = [
  {
    id: 1,
    name: '居民身份证办理',
    icon: '📄',
    desc: '本市户籍居民首次申领、换领、补领居民身份证',
    dept: '公安局',
    duration: '5个工作日',
    count: '23,456',
    isHot: true,
    category: 'household',
    filterType: 'personal'
  },
  {
    id: 2,
    name: '营业执照办理',
    icon: '🏢',
    desc: '企业设立、变更、注销登记，全程网上办理',
    dept: '市场监督管理局',
    duration: '1个工作日',
    count: '18,923',
    isHot: true,
    category: 'business',
    filterType: 'enterprise'
  },
  {
    id: 3,
    name: '不动产登记',
    icon: '🏠',
    desc: '不动产首次登记、转移登记、变更登记、注销登记',
    dept: '自然资源和规划局',
    duration: '3个工作日',
    count: '12,456',
    category: 'estate',
    filterType: 'personal'
  },
  {
    id: 4,
    name: '机动车驾驶证补换领',
    icon: '🚗',
    desc: '驾驶证遗失补办、期满换证、损毁换证',
    dept: '公安局交警支队',
    duration: '1个工作日',
    count: '9,876',
    category: 'traffic',
    filterType: 'personal'
  },
  {
    id: 5,
    name: '住房公积金提取',
    icon: '🏦',
    desc: '购房提取、租房提取、离退休提取等',
    dept: '住房公积金管理中心',
    duration: '3个工作日',
    count: '8,765',
    category: 'fund',
    filterType: 'personal'
  },
  {
    id: 6,
    name: '基本医疗保险参保登记',
    icon: '🏥',
    desc: '城镇职工、城乡居民医疗保险参保登记',
    dept: '医疗保障局',
    duration: '即时办结',
    count: '7,654',
    category: 'social',
    filterType: 'personal'
  },
  {
    id: 7,
    name: '食品经营许可证',
    icon: '🍱',
    desc: '食品销售、餐饮服务经营者需办理的许可证',
    dept: '市场监督管理局',
    duration: '5个工作日',
    count: '6,543',
    category: 'business',
    filterType: 'enterprise'
  },
  {
    id: 8,
    name: '居住证办理',
    icon: '📝',
    desc: '非本市户籍人员在本市居住的居住登记和居住证办理',
    dept: '公安局',
    duration: '15个工作日',
    count: '5,432',
    category: 'household',
    filterType: 'personal'
  },
  {
    id: 9,
    name: '结婚登记',
    icon: '💑',
    desc: '内地居民结婚登记、补领婚姻登记证',
    dept: '民政局',
    duration: '即时办结',
    count: '4,321',
    category: 'marriage',
    filterType: 'personal'
  },
  {
    id: 10,
    name: '新生儿出生登记',
    icon: '👶',
    desc: '新生儿出生户口登记、出生医学证明办理',
    dept: '公安局',
    duration: '3个工作日',
    count: '3,210',
    category: 'birth',
    filterType: 'personal'
  },
  {
    id: 11,
    name: '社保卡申领',
    icon: '💳',
    desc: '社会保障卡首次申领、补卡、换卡',
    dept: '人力资源和社会保障局',
    duration: '10个工作日',
    count: '2,890',
    category: 'social',
    filterType: 'personal'
  },
  {
    id: 12,
    name: '企业变更登记',
    icon: '📝',
    desc: '企业名称、地址、经营范围等变更登记',
    dept: '市场监督管理局',
    duration: '3个工作日',
    count: '2,567',
    category: 'business',
    filterType: 'enterprise'
  }
]

// 获取列表数据
const fetchItemList = async () => {
  loading.value = true

  // 模拟接口请求
  await new Promise(resolve => setTimeout(resolve, 300))

  let filtered = [...mockItems]

  // 分类筛选
  if (activeCategory.value !== 'all') {
    filtered = filtered.filter(item => item.category === activeCategory.value)
  }

  // 主题筛选
  if (activeFilter.value !== 'all') {
    filtered = filtered.filter(item => item.filterType === activeFilter.value)
  }

  // 搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(item =>
      item.name.toLowerCase().includes(keyword) ||
      item.desc.toLowerCase().includes(keyword)
    )
  }

  // 排序
  if (activeSort.value === 'hot') {
    filtered.sort((a, b) => parseInt(b.count.replace(/,/g, '')) - parseInt(a.count.replace(/,/g, '')))
  }

  total.value = filtered.length
  itemList.value = filtered.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)

  loading.value = false
}

// ========== 分页 ==========
const handlePageChange = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchItemList()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// ========== 操作 ==========
const handleItemClick = (item) => {
  router.push(`/items/${item.id}`)
}

const handleApply = (item) => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/apply/${item.id}`)
}

// ========== 初始化 ==========
onMounted(() => {
  // 从 URL 获取分类参数
  const category = route.query.category
  if (category) {
    activeCategory.value = category
  }

  fetchItemList()
})
</script>

<style lang="scss" scoped>
.items-page {
  background: #F3F4F6;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 页面横幅 */
.page-banner {
  background: linear-gradient(135deg, #1E40AF 0%, #3B82F6 100%);
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

/* 搜索栏 */
.search-bar {
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  margin-top: -24px;
  position: relative;
  z-index: 10;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.search-row {
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
    border: 1px solid #E5E7EB;
    border-radius: 8px;
    font-size: 14px;
    outline: none;
    transition: border-color 0.2s;

    &:focus {
      border-color: #3B82F6;
    }

    &::placeholder {
      color: #9CA3AF;
    }
  }
}

.search-btn {
  width: 100px;
  height: 44px;
  background: #1E40AF;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #3B82F6;
  }
}

/* 主体布局 */
.main-layout {
  display: flex;
  gap: 24px;
  margin-top: 24px;
}

/* 左侧分类 */
.sidebar {
  width: 240px;
  flex-shrink: 0;
}

.category-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.category-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #1F2937;
  border-bottom: 1px solid #F3F4F6;
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-list {
  padding: 8px 0;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 10px;

  &:hover {
    background: #F9FAFB;
  }

  &.active {
    background: #EFF6FF;
    color: #1E40AF;
    font-weight: 500;
  }
}

.cat-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.cat-name {
  flex: 1;
  font-size: 14px;
}

.cat-count {
  font-size: 12px;
  color: #6B7280;
}

.category-item.active .cat-count {
  color: #1E40AF;
}

/* 右侧内容 */
.content {
  flex: 1;
  min-width: 0;
}

/* 筛选条件 */
.filter-bar {
  background: white;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.filter-label {
  font-size: 13px;
  color: #6B7280;
  flex-shrink: 0;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: #F3F4F6;
  color: #374151;

  &:hover {
    color: #1E40AF;
  }

  &.active {
    background: #1E40AF;
    color: white;
  }
}

/* 结果统计和排序 */
.result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.result-count {
  font-size: 14px;
  color: #6B7280;

  span {
    color: #1E40AF;
    font-weight: 600;
  }
}

.sort-options {
  display: flex;
  gap: 20px;
}

.sort-item {
  font-size: 13px;
  color: #6B7280;
  cursor: pointer;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;

  &:hover {
    color: #374151;
  }

  &.active {
    color: #1E40AF;
    border-bottom-color: #1E40AF;
  }
}

/* 事项列表 */
.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 400px;
}

.item-card {
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  &:hover {
    border-color: #93C5FD;
    box-shadow: 0 4px 12px rgba(30, 64, 175, 0.1);
    transform: translateX(4px);
  }
}

.item-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
  background: #EFF6FF;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hot-badge {
  font-size: 11px;
  padding: 2px 6px;
  background: #FEE2E2;
  color: #EF4444;
  border-radius: 4px;
  font-weight: 500;
}

.item-desc {
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #6B7280;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

.item-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 20px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.item-btn-primary {
  background: #1E40AF;
  color: white;
  border: none;

  &:hover {
    background: #3B82F6;
  }
}

.item-btn-outline {
  background: transparent;
  color: #1E40AF;
  border: 1px solid #1E40AF;

  &:hover {
    background: #EFF6FF;
  }
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 0;
  color: #6B7280;

  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #F3F4F6;
    border-top-color: #1E40AF;
    border-radius: 50%;
    margin: 0 auto 16px;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 分页 */
.pagination-wrap {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #E5E7EB;
  background: white;
  padding: 0 8px;

  &:hover:not(.disabled):not(.ellipsis) {
    color: #1E40AF;
    border-color: #1E40AF;
  }

  &.active {
    background: #1E40AF;
    color: white;
    border-color: #1E40AF;
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &.ellipsis {
    border: none;
    cursor: default;
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
    color: #1F2937;
    margin-bottom: 8px;
  }

  .empty-desc {
    font-size: 14px;
    color: #6B7280;
  }
}

/* 响应式 */
@media (max-width: 1024px) {
  .main-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .category-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px;
    max-height: none;
  }

  .category-item {
    padding: 8px 16px;
    border-radius: 20px;
    background: #F3F4F6;
  }

  .category-item.active {
    background: #1E40AF;
    color: white;
  }

  .category-item .cat-count {
    display: none;
  }
}

@media (max-width: 768px) {
  .search-row {
    flex-direction: column;

    .search-btn {
      width: 100%;
    }
  }

  .item-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .item-actions {
    flex-direction: row;
    width: 100%;

    .item-btn {
      flex: 1;
    }
  }
}
</style>
