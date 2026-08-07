<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-illustration">
        <div class="error-code">404</div>
        <div class="error-icon">🔍</div>
      </div>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移除</p>
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><Back /></el-icon>
          返回上页
        </el-button>
      </div>
      <div class="error-suggestions">
        <p>您可以尝试：</p>
        <ul>
          <li>检查网址是否输入正确</li>
          <li><router-link to="/">回到首页</router-link>重新查找</li>
          <li><router-link to="/items">浏览事项列表</router-link></li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 24px;
}

.error-content {
  text-align: center;
  max-width: 480px;
}

.error-illustration {
  position: relative;
  margin-bottom: 32px;
}

.error-code {
  font-size: 120px;
  font-weight: 800;
  color: transparent;
  background: linear-gradient(135deg, #1E40AF 0%, #3B82F6 100%);
  -webkit-background-clip: text;
  background-clip: text;
  line-height: 1;
  margin-bottom: 16px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.error-icon {
  font-size: 64px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(-50%, -50%); }
  50% { transform: translate(-50%, calc(-50% - 10px)); }
}

.error-title {
  font-size: 28px;
  font-weight: 700;
  color: #1F2937;
  margin-bottom: 12px;
}

.error-desc {
  font-size: 16px;
  color: #6B7280;
  margin-bottom: 32px;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 48px;

  .el-button {
    padding: 12px 28px;
  }
}

.error-suggestions {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: left;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  p {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
  }

  ul {
    list-style: none;
    padding: 0;
    margin: 0;
  }

  li {
    font-size: 14px;
    color: #6B7280;
    padding: 8px 0;
    position: relative;
    padding-left: 20px;

    &::before {
      content: '•';
      position: absolute;
      left: 0;
      color: #9CA3AF;
    }

    a {
      color: #3B82F6;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

/* 响应式 */
@media (max-width: 480px) {
  .error-code {
    font-size: 80px;
  }

  .error-icon {
    font-size: 48px;
  }

  .error-title {
    font-size: 22px;
  }

  .error-actions {
    flex-direction: column;
  }
}
</style>
