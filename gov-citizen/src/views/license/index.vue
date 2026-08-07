<template>
  <div class="license-page">
    <!-- 页面横幅 -->
    <section class="page-banner">
      <div class="container">
        <h2>我的证照</h2>
        <p>管理您的电子证照，支持下载和验证</p>
      </div>
    </section>

    <div class="page-container">
      <div class="container">
        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-icon">📜</div>
            <div class="stat-number">{{ stats.total }}</div>
            <div class="stat-label">证照总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">✅</div>
            <div class="stat-number">{{ stats.valid }}</div>
            <div class="stat-label">有效证照</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">⚠️</div>
            <div class="stat-number">{{ stats.expiring }}</div>
            <div class="stat-label">即将过期</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">📥</div>
            <div class="stat-number">{{ stats.downloads }}</div>
            <div class="stat-label">下载次数</div>
          </div>
        </div>

        <!-- 证照分类 -->
        <div class="category-tabs">
          <span
            v-for="cat in categories"
            :key="cat.id"
            :class="['category-tab', { active: activeCategory === cat.id }]"
            @click="handleCategoryChange(cat.id)"
          >
            {{ cat.name }}
          </span>
        </div>

        <!-- 证照列表 -->
        <div v-if="filteredList.length > 0" class="license-grid">
          <div
            v-for="item in filteredList"
            :key="item.id"
            class="license-card"
            @click="showDetail(item)"
          >
            <div class="license-preview" :style="{ background: item.gradient }">
              <span class="preview-icon">{{ item.icon }}</span>
              <span :class="['license-type-badge', `badge-${item.type}`]">
                {{ getTypeName(item.type) }}
              </span>
              <span v-if="item.status === 'expiring'" class="expire-warning">
                即将过期
              </span>
            </div>
            <div class="license-info">
              <div class="license-name">{{ item.name }}</div>
              <div class="license-meta">
                <div class="license-meta-item">
                  <span class="label">{{ item.type === 'business' ? '统一信用代码：' : '证照号：' }}</span>
                  <span class="value">{{ item.number }}</span>
                </div>
                <div class="license-meta-item">
                  <span class="label">有效期：</span>
                  <span :class="['value', { 'text-warning': item.status === 'expiring' }]">
                    {{ item.validPeriod }}
                  </span>
                </div>
                <div class="license-meta-item">
                  <span class="label">状态：</span>
                  <span :class="['value', `status-${item.status}`]">
                    {{ getStatusText(item.status) }}
                  </span>
                </div>
              </div>
              <div class="license-actions">
                <button class="license-btn btn-download" @click.stop="handleDownload(item)">
                  📥 下载
                </button>
                <button class="license-btn btn-verify" @click.stop="handleVerify(item)">
                  🔍 验证
                </button>
                <button class="license-btn btn-view" @click.stop="showDetail(item)">
                  👁️ 查看
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-icon">📋</div>
          <div class="empty-title">暂无证照</div>
          <div class="empty-desc">您还没有相关的电子证照</div>
        </div>
      </div>
    </div>

    <!-- 证照详情弹窗 -->
    <el-dialog
      v-model="showModal"
      :title="currentLicense?.name || '证照详情'"
      width="600px"
      class="license-dialog"
    >
      <div class="license-detail-preview" :style="{ background: currentLicense?.gradient }">
        <div class="preview-content">
          <div class="preview-icon">{{ currentLicense?.icon }}</div>
          <div class="preview-text">{{ currentLicense?.name }}</div>
        </div>
      </div>
      <div class="detail-info">
        <div v-for="(value, key) in currentLicense?.details" :key="key" class="detail-item">
          <span class="label">{{ key }}</span>
          <span class="value">{{ value }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showModal = false">关闭</el-button>
        <el-button type="primary" @click="handleDownload(currentLicense)">
          📥 下载证照
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

// ========== 统计数据 ==========
const stats = ref({
  total: 5,
  valid: 4,
  expiring: 1,
  downloads: 12
})

// ========== 分类 ==========
const activeCategory = ref('all')

const categories = [
  { id: 'all', name: '全部证照' },
  { id: 'id', name: '身份证件' },
  { id: 'driving', name: '驾驶证照' },
  { id: 'business', name: '营业执照' },
  { id: 'house', name: '不动产证' }
]

const handleCategoryChange = (catId) => {
  activeCategory.value = catId
}

const getTypeName = (type) => {
  const map = {
    id: '身份证件',
    driving: '驾驶证照',
    business: '营业执照',
    house: '不动产证'
  }
  return map[type] || type
}

// ========== 证照列表 ==========
const licenseList = ref([
  {
    id: 1,
    name: '居民身份证',
    icon: '🪪',
    type: 'id',
    number: '460100199001011234',
    validPeriod: '2020-01-01 至 2030-01-01',
    status: 'valid',
    gradient: 'linear-gradient(135deg, #1E40AF 0%, #3B82F6 100%)',
    details: {
      '姓名': '张三',
      '性别': '男',
      '民族': '汉族',
      '出生日期': '1990年01月01日',
      '住址': '海南省海口市龙华区XX路XX号',
      '身份证号': '460100199001011234',
      '签发机关': '海口市公安局龙华分局',
      '有效期': '2020-01-01 至 2030-01-01'
    }
  },
  {
    id: 2,
    name: '机动车驾驶证',
    icon: '🚗',
    type: 'driving',
    number: '460100199001011234',
    validPeriod: '2022-05-15 至 2032-05-15',
    status: 'valid',
    gradient: 'linear-gradient(135deg, #059669 0%, #10B981 100%)',
    details: {
      '姓名': '张三',
      '证号': '460100199001011234',
      '准驾车型': 'C1',
      '初次领证日期': '2012-05-15',
      '有效期限': '2022-05-15 至 2032-05-15',
      '发证机关': '海南省海口市公安局交通警察支队'
    }
  },
  {
    id: 3,
    name: '营业执照',
    icon: '🏢',
    type: 'business',
    number: '91460100MA5TXXXXXX',
    validPeriod: '2023-08-20 至 长期',
    status: 'valid',
    gradient: 'linear-gradient(135deg, #D97706 0%, #F59E0B 100%)',
    details: {
      '企业名称': '海口XX科技有限公司',
      '统一社会信用代码': '91460100MA5TXXXXXX',
      '法定代表人': '张三',
      '注册资本': '100万元',
      '成立日期': '2023-08-20',
      '营业期限': '2023-08-20 至 长期',
      '登记机关': '海口市市场监督管理局'
    }
  },
  {
    id: 4,
    name: '不动产权证书',
    icon: '🏠',
    type: 'house',
    number: '琼(2023)海口市不动产权第XXXX号',
    validPeriod: '长期有效',
    status: 'valid',
    gradient: 'linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)',
    details: {
      '权利人': '张三',
      '共有情况': '单独所有',
      '坐落': '海口市龙华区XX小区X栋X单元XXX',
      '不动产单元号': '460100001001GB00001F00010001',
      '权利类型': '国有建设用地使用权/房屋所有权',
      '登记时间': '2023-12-01',
      '证号': '琼(2023)海口市不动产权第XXXX号'
    }
  },
  {
    id: 5,
    name: '居住证',
    icon: '📋',
    type: 'id',
    number: '460100202301XXXXXX',
    validPeriod: '2023-06-01 至 2024-06-01',
    status: 'expiring',
    gradient: 'linear-gradient(135deg, #DC2626 0%, #EF4444 100%)',
    details: {
      '姓名': '张三',
      '性别': '男',
      '公民身份号码': '460100199001011234',
      '居住地住址': '海南省海口市龙华区XX路XX号',
      '签发机关': '海口市公安局龙华分局',
      '签发日期': '2023-06-01',
      '有效期限': '2023-06-01 至 2024-06-01'
    }
  }
])

// 筛选后的列表
const filteredList = computed(() => {
  if (activeCategory.value === 'all') {
    return licenseList.value
  }
  return licenseList.value.filter(item => item.type === activeCategory.value)
})

// ========== 状态 ==========
const getStatusText = (status) => {
  const map = {
    valid: '有效',
    expiring: '即将过期',
    expired: '已过期'
  }
  return map[status] || status
}

// ========== 详情弹窗 ==========
const showModal = ref(false)
const currentLicense = ref(null)

const showDetail = (item) => {
  currentLicense.value = item
  showModal.value = true
}

// ========== 操作 ==========
const handleDownload = (item) => {
  if (!item) return
  ElMessage.success(`正在下载: ${item.name}`)
}

const handleVerify = (item) => {
  ElMessage.success(`${item.name} 验证通过`)
}
</script>

<style lang="scss" scoped>
.license-page {
  min-height: 100vh;
  background: #F3F4F6;
}

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

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: -32px;
  margin-bottom: 32px;
  position: relative;
  z-index: 10;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1E40AF;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6B7280;
}

/* 证照分类 */
.category-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.category-tab {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
  border: 1px solid #E5E7EB;
  color: #4B5563;

  &:hover {
    border-color: #93C5FD;
    color: #1E40AF;
  }

  &.active {
    background: #1E40AF;
    border-color: #1E40AF;
    color: white;
  }
}

/* 证照网格 */
.license-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.license-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.license-preview {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.preview-icon {
  font-size: 64px;
  opacity: 0.8;
}

.license-type-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;

  &.badge-id {
    background: #EFF6FF;
    color: #1E40AF;
  }

  &.badge-driving {
    background: #D1FAE5;
    color: #059669;
  }

  &.badge-business {
    background: #FEF3C7;
    color: #D97706;
  }

  &.badge-house {
    background: #E0E7FF;
    color: #4F46E5;
  }
}

.expire-warning {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  background: #FEF3C7;
  color: #D97706;
}

.license-info {
  padding: 20px;
}

.license-name {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 12px;
}

.license-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.license-meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;

  .label {
    color: #6B7280;
    width: 80px;
    flex-shrink: 0;
  }

  .value {
    color: #1F2937;

    &.text-warning {
      color: #D97706;
    }

    &.status-valid {
      color: #059669;
    }

    &.status-expiring {
      color: #D97706;
    }

    &.status-expired {
      color: #DC2626;
    }
  }
}

.license-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #F3F4F6;
}

.license-btn {
  flex: 1;
  height: 36px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
}

.btn-download {
  background: #1E40AF;
  color: white;

  &:hover {
    background: #1E3A8A;
  }
}

.btn-verify {
  background: #EFF6FF;
  color: #1E40AF;

  &:hover {
    background: #DBEAFE;
  }
}

.btn-view {
  background: #F3F4F6;
  color: #4B5563;

  &:hover {
    background: #E5E7EB;
  }
}

/* 详情弹窗 */
.license-detail-preview {
  height: 300px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.preview-content {
  text-align: center;
  color: white;

  .preview-icon {
    font-size: 80px;
    margin-bottom: 16px;
    opacity: 1;
  }

  .preview-text {
    font-size: 16px;
    opacity: 0.9;
  }
}

.detail-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .label {
    font-size: 13px;
    color: #6B7280;
  }

  .value {
    font-size: 14px;
    color: #1F2937;
    font-weight: 500;
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
  .license-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .license-grid {
    grid-template-columns: 1fr;
  }

  .detail-info {
    grid-template-columns: 1fr;
  }
}
</style>
