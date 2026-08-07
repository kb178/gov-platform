<template>
  <div class="item-detail-page">
    <div class="page-container">
      <div class="container">
        <!-- 面包屑 -->
        <div class="breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="separator">/</span>
          <router-link to="/items">事项办理</router-link>
          <span class="separator">/</span>
          <router-link :to="`/items?category=${itemDetail.category}`">{{
            itemDetail.categoryName
          }}</router-link>
          <span class="separator">/</span>
          <span class="current">{{ itemDetail.name }}</span>
        </div>

        <!-- 主体布局 -->
        <div class="detail-layout">
          <!-- 左侧内容 -->
          <div class="detail-main">
            <!-- 事项头部 -->
            <div class="item-header">
              <div class="item-header-top">
                <div class="item-icon-large">{{ itemDetail.icon }}</div>
                <div class="item-title-area">
                  <h1>{{ itemDetail.name }}</h1>
                  <div class="item-tags">
                    <span class="item-tag dept">{{ itemDetail.dept }}</span>
                    <span class="item-tag type">{{ itemDetail.categoryName }}</span>
                  </div>
                  <div class="item-meta-info">
                    <span>⏱️ 办理时限：{{ itemDetail.duration }}</span>
                    <span>📊 已办理 {{ itemDetail.count }} 件</span>
                    <span>⭐ 好评率 {{ itemDetail.rating }}%</span>
                  </div>
                </div>
              </div>
              <div class="item-actions-bar">
                <button class="btn-apply" @click="handleApply">立即办理</button>
                <button :class="['btn-action', { collected: isCollected }]" @click="toggleCollect">
                  <span>{{ isCollected ? '★' : '☆' }}</span>
                  {{ isCollected ? '已收藏' : '收藏' }}
                </button>
                <button class="btn-action" @click="handleShare"><span>📤</span> 分享</button>
              </div>
            </div>

            <!-- 办理条件 -->
            <div class="content-section">
              <h3 class="section-title">
                <span class="icon">📋</span>
                办理条件
              </h3>
              <ul class="condition-list">
                <li v-for="(condition, index) in itemDetail.conditions" :key="index">
                  {{ condition }}
                </li>
              </ul>
            </div>

            <!-- 所需材料 -->
            <div class="content-section">
              <h3 class="section-title">
                <span class="icon">📎</span>
                所需材料
              </h3>
              <table class="material-table">
                <thead>
                  <tr>
                    <th>序号</th>
                    <th>材料名称</th>
                    <th>要求</th>
                    <th>备注</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(material, index) in itemDetail.materials" :key="index">
                    <td>{{ index + 1 }}</td>
                    <td>{{ material.name }}</td>
                    <td>
                      <span :class="material.required ? 'material-required' : 'material-optional'">
                        {{ material.required ? '必需' : '可选' }}
                      </span>
                    </td>
                    <td>
                      {{ material.note }}
                      <div v-if="material.tip" class="material-tip">{{ material.tip }}</div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- 办理流程 -->
            <div class="content-section">
              <h3 class="section-title">
                <span class="icon">🔄</span>
                办理流程
              </h3>
              <div class="flow-steps">
                <div v-for="(step, index) in itemDetail.steps" :key="index" class="flow-step">
                  <div class="step-number">{{ index + 1 }}</div>
                  <div class="step-content">
                    <div class="step-title">{{ step.title }}</div>
                    <div class="step-desc">{{ step.desc }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 办理地点 -->
            <div class="content-section">
              <h3 class="section-title">
                <span class="icon">📍</span>
                办理地点
              </h3>
              <div class="location-card">
                <div class="location-icon">🏛️</div>
                <div class="location-info">
                  <h4>{{ itemDetail.location.name }}</h4>
                  <p>
                    <span class="address">📍 {{ itemDetail.location.address }}</span
                    ><br />
                    📞 电话：{{ itemDetail.location.phone }}<br />
                    🕐 工作时间：{{ itemDetail.location.hours }}<br />
                    🚌 交通：{{ itemDetail.location.traffic }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧边栏 -->
          <div class="detail-sidebar">
            <!-- 办理信息 -->
            <div class="info-card">
              <h3>📌 办理信息</h3>
              <div class="info-item">
                <span class="info-label">办理时限</span>
                <span class="info-value highlight">{{ itemDetail.duration }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">收费标准</span>
                <span class="info-value">{{ itemDetail.fee }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">办理方式</span>
                <span class="info-value">{{ itemDetail.method }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">咨询电话</span>
                <span class="info-value highlight">{{ itemDetail.phone }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">监督电话</span>
                <span class="info-value">{{ itemDetail.supervisorPhone }}</span>
              </div>
            </div>

            <!-- 常见问题 -->
            <div class="info-card">
              <h3>❓ 常见问题</h3>
              <div class="faq-list">
                <div
                  v-for="(faq, index) in itemDetail.faqs"
                  :key="index"
                  class="faq-item"
                  @click="handleFaqClick(faq)"
                >
                  <div class="faq-question">{{ faq.question }}</div>
                </div>
              </div>
            </div>

            <!-- 相关事项 -->
            <div class="info-card">
              <h3>🔗 相关事项</h3>
              <div class="related-list">
                <div
                  v-for="related in itemDetail.relatedItems"
                  :key="related.id"
                  class="related-item"
                  @click="goToDetail(related.id)"
                >
                  <div class="related-icon">{{ related.icon }}</div>
                  <span class="related-name">{{ related.name }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 收藏状态
const isCollected = ref(false)

// 模拟事项详情数据
const itemDetail = ref({
  id: 1,
  name: '居民身份证办理',
  icon: '📄',
  dept: '公安局',
  category: 'household',
  categoryName: '户籍办理',
  duration: '5个工作日',
  count: '23,456',
  rating: 98.5,
  fee: '首次申领免费，换领20元，补领40元',
  method: '网上预约 + 线下办理',
  phone: '0898-12345',
  supervisorPhone: '0898-6666XXXX',
  conditions: [
    '具有海口市户籍的居民',
    '年满16周岁的中国公民',
    '未满16周岁的公民，由监护人代为申请',
    '首次申领、换领、补领居民身份证'
  ],
  materials: [
    { name: '居民户口簿', required: true, note: '原件' },
    { name: '本人近期免冠照片', required: true, note: '白底彩色照片', tip: '可现场拍摄' },
    { name: '原居民身份证', required: false, note: '换领时需要' },
    { name: '监护人身份证', required: false, note: '未满16周岁需要' }
  ],
  steps: [
    { title: '在线申请', desc: '登录政务服务平台，在线填写申请表，上传相关材料照片' },
    { title: '窗口受理', desc: '工作人员审核材料，确认无误后受理申请' },
    { title: '人像采集', desc: '到指定地点进行人像采集和指纹录入' },
    { title: '证件制作', desc: '公安机关制作居民身份证' },
    { title: '领取证件', desc: '到受理点领取或选择邮寄送达' }
  ],
  location: {
    name: '海口市公安局户政大厅',
    address: '海口市龙华区滨海大道XXX号',
    phone: '0898-6666XXXX',
    hours: '周一至周五 9:00-12:00 14:00-17:00',
    traffic: '乘坐XX路公交到XX站下车'
  },
  faqs: [
    { question: '身份证办理需要多长时间？', answer: '一般需要5个工作日。' },
    { question: '可以异地办理身份证吗？', answer: '可以，但需要提供相关证明材料。' },
    { question: '身份证丢失如何补办？', answer: '携带户口簿到户籍所在地派出所办理。' },
    { question: '临时身份证怎么办理？', answer: '到户籍所在地派出所申请，当天可取。' }
  ],
  relatedItems: [
    { id: 2, name: '户口迁移', icon: '🏠' },
    { id: 3, name: '居住证办理', icon: '📋' },
    { id: 4, name: '出生登记', icon: '👶' }
  ]
})

// 立即办理
const handleApply = () => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/apply/${route.params.id}`)
}

// 收藏切换
const toggleCollect = () => {
  isCollected.value = !isCollected.value
  ElMessage.success(isCollected.value ? '收藏成功' : '已取消收藏')
}

// 分享
const handleShare = () => {
  ElMessage.info('分享功能开发中')
}

// 常见问题点击
const handleFaqClick = faq => {
  ElMessage.info(faq.answer)
}

// 跳转相关事项
const goToDetail = id => {
  router.push(`/items/${id}`)
}

// 初始化
onMounted(() => {
  // 实际项目中根据 route.params.id 获取数据
  console.log('事项ID:', route.params.id)
})
</script>

<style lang="scss" scoped>
.item-detail-page {
  min-height: 100vh;
}

.page-container {
  padding: 0 0 48px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

/* 面包屑 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
  padding: 24px 0;

  a {
    color: #6b7280;
    text-decoration: none;
    transition: color 0.2s;

    &:hover {
      color: #1e40af;
    }
  }

  .separator {
    color: #d1d5db;
  }

  .current {
    color: #1f2937;
  }
}

/* 主体布局 */
.detail-layout {
  display: flex;
  gap: 24px;
}

/* 左侧内容 */
.detail-main {
  flex: 1;
  min-width: 0;
}

/* 事项头部 */
.item-header {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.item-header-top {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 24px;
}

.item-icon-large {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  flex-shrink: 0;
}

.item-title-area {
  flex: 1;

  h1 {
    font-size: 24px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 12px;
  }
}

.item-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.item-tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 4px;

  &.dept {
    background: #e0e7ff;
    color: #6366f1;
  }

  &.type {
    background: #eff6ff;
    color: #1e40af;
  }
}

.item-meta-info {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #6b7280;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.item-actions-bar {
  display: flex;
  gap: 12px;
}

.btn-apply {
  height: 48px;
  padding: 0 40px;
  font-size: 16px;
  font-weight: 600;
  background: #1e40af;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #3b82f6;
  }
}

.btn-action {
  height: 48px;
  padding: 0 24px;
  background: white;
  border: 1px solid #e5e7eb;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;

  &:hover {
    border-color: #93c5fd;
    color: #1e40af;
  }

  &.collected {
    color: #f59e0b;
    border-color: #f59e0b;
  }
}

/* 内容区块 */
.content-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;

  .icon {
    font-size: 22px;
  }
}

/* 办理条件 */
.condition-list {
  list-style: none;
  padding: 0;

  li {
    position: relative;
    padding: 12px 0 12px 28px;
    font-size: 14px;
    line-height: 1.8;
    color: #374151;

    &::before {
      content: '✓';
      position: absolute;
      left: 0;
      top: 14px;
      color: #10b981;
      font-weight: 600;
    }
  }
}

/* 所需材料 */
.material-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #f3f4f6;
  border-radius: 8px;
  overflow: hidden;

  th,
  td {
    padding: 14px 16px;
    text-align: left;
    border-bottom: 1px solid #f3f4f6;
  }

  th {
    background: #f9fafb;
    font-weight: 600;
    font-size: 13px;
    color: #6b7280;
  }

  tr:last-child td {
    border-bottom: none;
  }

  td {
    font-size: 14px;
    color: #374151;
  }
}

.material-required {
  color: #ef4444;
  font-weight: 500;
}

.material-optional {
  color: #6b7280;
}

.material-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

/* 办理流程 */
.flow-steps {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.flow-step {
  display: flex;
  gap: 20px;
  position: relative;

  &:not(:last-child) {
    padding-bottom: 32px;
  }

  &:not(:last-child)::before {
    content: '';
    position: absolute;
    left: 19px;
    top: 40px;
    bottom: 0;
    width: 2px;
    background: #f3f4f6;
  }
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #1e40af;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.step-content {
  flex: 1;
  padding-top: 8px;
}

.step-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.step-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
}

/* 办理地点 */
.location-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
}

.location-icon {
  width: 48px;
  height: 48px;
  background: #eff6ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.location-info {
  h4 {
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
  }

  p {
    font-size: 13px;
    color: #6b7280;
    line-height: 1.8;
  }

  .address {
    color: #3b82f6;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
}

/* 右侧边栏 */
.detail-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.info-item {
  display: flex;
  padding: 14px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 14px;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  width: 80px;
  color: #6b7280;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #1f2937;

  &.highlight {
    color: #1e40af;
    font-weight: 500;
  }
}

/* 常见问题 */
.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-item {
  padding: 14px 16px;
  background: #f9fafb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #eff6ff;
  }
}

.faq-question {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  display: flex;
  align-items: center;
  justify-content: space-between;

  &::after {
    content: '>';
    color: #9ca3af;
  }
}

/* 相关事项 */
.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f9fafb;
  }
}

.related-icon {
  width: 36px;
  height: 36px;
  background: #eff6ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.related-name {
  font-size: 14px;
  color: #374151;
}

/* 响应式 */
@media (max-width: 1024px) {
  .detail-layout {
    flex-direction: column;
  }

  .detail-sidebar {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .item-header-top {
    flex-direction: column;
  }

  .item-meta-info {
    flex-wrap: wrap;
    gap: 12px;
  }

  .item-actions-bar {
    flex-direction: column;

    .btn-apply,
    .btn-action {
      width: 100%;
      justify-content: center;
    }
  }

  .location-card {
    flex-direction: column;
  }
}
</style>
