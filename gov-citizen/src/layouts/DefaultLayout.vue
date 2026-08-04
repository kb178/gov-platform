<template>
  <div class="default-layout">
    <!-- 顶部导航 -->
    <header class="layout-header">
      <div class="header-content">
        <div class="logo">
          <img src="@/assets/logo.svg" alt="logo" />
          <span>海口政务服务平台</span>
        </div>
        <nav class="nav-menu">
          <router-link to="/">首页</router-link>
        </nav>
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown>
              <span class="user-info">
                <el-icon><User /></el-icon>
                {{ userStore.username }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">
              登录
            </el-button>
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
      <p>© 2024 海口市政务服务管理局</p>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { User } from '@element-plus/icons-vue'

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

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1E40AF;

  img {
    height: 32px;
  }
}

.nav-menu {
  display: flex;
  gap: 24px;

  a {
    color: #333;
    text-decoration: none;

    &.router-link-active {
      color: #1E40AF;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.layout-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

.layout-footer {
  padding: 24px;
  text-align: center;
  background: #f5f5f5;
  color: #666;
}
</style>
