<template>
  <div class="item-detail-page">
    <div class="page-container">
      <el-page-header @back="goBack">
        <template #content>
          <span class="page-title">事项详情</span>
        </template>
      </el-page-header>

      <div class="detail-content">
        <el-card>
          <div class="placeholder">
            <div class="placeholder-icon">📄</div>
            <h3>事项详情页开发中...</h3>
            <p>此页面将展示事项的详细信息、办理条件、所需材料、办理流程等</p>
            <el-button type="primary" @click="goBack">返回列表</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/items')
}
</script>

<style lang="scss" scoped>
.item-detail-page {
  min-height: 400px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.detail-content {
  margin-top: 24px;
}

.placeholder {
  text-align: center;
  padding: 80px 40px;

  .placeholder-icon {
    font-size: 64px;
    margin-bottom: 24px;
  }

  h3 {
    font-size: 20px;
    color: var(--text-primary);
    margin-bottom: 12px;
  }

  p {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 32px;
  }
}
</style>
