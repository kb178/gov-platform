<template>
  <div class="default-layout">
    <!-- 顶部导航 -->
    <header class="layout-header">
      <div class="header-content">
        <router-link to="/" class="header-logo">
          <div class="logo-icon">🏛️</div>
          <span>海口政务服务</span>
        </router-link>

        <nav class="nav-menu">
          <router-link to="/" exact-active-class="active">首页</router-link>
          <router-link to="/items" active-class="active">事项办理</router-link>
          <router-link to="/progress" active-class="active">进度查询</router-link>
          <router-link to="/my-license" active-class="active">我的证照</router-link>
        </nav>

        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown>
              <span class="user-info">
                <div class="user-avatar">{{ userStore.username?.charAt(0) || '用' }}</div>
                <span class="username">{{ userStore.username || '用户' }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/message')">
                    <el-icon><Bell /></el-icon>
                    消息通知
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="layout-main">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="layout-footer">
      <div class="footer-content">
        <div class="footer-section">
          <h4>关于我们</h4>
          <p>海口市政务服务平台</p>
          <p>打造智慧政务，服务百姓生活</p>
        </div>
        <div class="footer-section">
          <h4>联系方式</h4>
          <p>服务热线：0898-12345</p>
          <p>工作时间：周一至周五 9:00-17:00</p>
        </div>
        <div class="footer-section">
          <h4>友情链接</h4>
          <a href="#">海南省人民政府</a>
          <a href="#">海口市人民政府</a>
          <a href="#">国家政务服务平台</a>
        </div>
        <div class="footer-section">
          <h4>帮助中心</h4>
          <a href="#">常见问题</a>
          <a href="#">操作指南</a>
          <a href="#">意见反馈</a>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2024 海口市政务服务平台 版权所有 | 琼ICP备XXXXXXXX号</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { User, Bell, SwitchButton } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = async () => {
  await userStore.logoutAction()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.default-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 头部导航 */
.layout-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  height: 64px;
  margin: 0 auto;
  padding: 0 24px;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1E40AF;
  text-decoration: none;

  .logo-icon {
    width: 36px;
    height: 36px;
    background: linear-gradient(135deg, #1E40AF, #3B82F6);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
  }
}

.nav-menu {
  display: flex;
  gap: 32px;

  a {
    color: #333;
    text-decoration: none;
    font-size: 15px;
    padding: 8px 0;
    position: relative;
    transition: color 0.2s;

    &:hover {
      color: #1E40AF;
    }

    &.active {
      color: #1E40AF;
      font-weight: 500;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 2px;
        background: #1E40AF;
        border-radius: 1px;
      }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: #f5f5f5;
  }
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #93C5FD, #3B82F6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.username {
  font-size: 14px;
  color: #333;
}

/* 主内容区 */
.layout-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

/* 底部 */
.layout-footer {
  background: #1F2937;
  color: rgba(255, 255, 255, 0.7);
  padding: 48px 0 24px;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  gap: 48px;
  margin-bottom: 32px;
}

.footer-section {
  h4 {
    color: white;
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 16px;
  }

  p {
    font-size: 13px;
    line-height: 2;
    color: rgba(255, 255, 255, 0.6);
  }

  a {
    display: block;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.6);
    text-decoration: none;
    line-height: 2;
    transition: color 0.2s;

    &:hover {
      color: white;
    }
  }
}

.footer-bottom {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;

  p {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.4);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }

  .footer-content {
    flex-direction: column;
    gap: 24px;
  }
}
</style>
