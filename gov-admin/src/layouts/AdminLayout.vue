<template>
  <div class="admin-layout">
    <!-- 左侧菜单 -->
    <aside class="layout-aside" :class="{ 'is-collapse': isCollapse }">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="logo" />
        <span v-show="!isCollapse">政务管理后台</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>工作台</span>
          </el-menu-item>

          <el-sub-menu index="/approval">
            <template #title>
              <el-icon><Tickets /></el-icon>
              <span>审批管理</span>
            </template>
            <el-menu-item index="/approval/pending">待我审批</el-menu-item>
            <el-menu-item index="/approval/done">已办事项</el-menu-item>
            <el-menu-item index="/approval/monitor">流程监控</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/item">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>事项管理</span>
            </template>
            <el-menu-item index="/item/category">事项分类</el-menu-item>
            <el-menu-item index="/item/list">事项列表</el-menu-item>
            <el-menu-item index="/item/guide">办事指南</el-menu-item>
            <el-menu-item index="/item/template">表单模板</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/license">
            <template #title>
              <el-icon><Stamp /></el-icon>
              <span>证照管理</span>
            </template>
            <el-menu-item index="/license/template">证照模板</el-menu-item>
            <el-menu-item index="/license/list">证照列表</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/statistics">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </template>
            <el-menu-item index="/statistics/overview">办件统计</el-menu-item>
            <el-menu-item index="/statistics/screen">数据大屏</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user">用户管理</el-menu-item>
            <el-menu-item index="/system/role">角色管理</el-menu-item>
            <el-menu-item index="/system/dept">部门管理</el-menu-item>
            <el-menu-item index="/system/menu">菜单管理</el-menu-item>
            <el-menu-item index="/system/dict">字典管理</el-menu-item>
            <el-menu-item index="/system/log">操作日志</el-menu-item>
            <el-menu-item index="/system/config">系统参数</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/message">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>消息中心</span>
            </template>
            <el-menu-item index="/message/notice">系统公告</el-menu-item>
            <el-menu-item index="/message/list">站内消息</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </aside>

    <!-- 右侧内容区 -->
    <div class="layout-main">
      <!-- 顶部导航栏 -->
      <header class="layout-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            @click="isCollapse = !isCollapse"
          >
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.avatar">
                {{ userStore.nickname?.charAt(0) || 'A' }}
              </el-avatar>
              <span>{{ userStore.nickname || '管理员' }}</span>
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
        </div>
      </header>

      <!-- 主内容区 -->
      <main class="layout-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Odometer,
  Tickets,
  Document,
  Stamp,
  DataAnalysis,
  Setting,
  Bell,
  Expand,
  Fold
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 退出登录
const handleLogout = async () => {
  await userStore.logoutAction()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.layout-aside {
  width: 220px;
  height: 100vh;
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
  flex-shrink: 0;

  &.is-collapse {
    width: 64px;
  }
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  padding: 0 16px;
  background: #2b3648;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;

  img {
    width: 32px;
    height: 32px;
    margin-right: 12px;
  }
}

:deep(.el-menu) {
  border-right: none;
  background: #304156;

  .el-menu-item,
  .el-sub-menu__title {
    color: #bfcbd9;

    &:hover {
      background: #263445;
    }
  }

  .el-menu-item.is-active {
    color: #409eff;
    background: #263445;
  }
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;

  &:hover {
    color: #409eff;
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

.layout-content {
  flex: 1;
  padding: 24px;
  background: #f5f7fa;
  overflow-y: auto;
  height: 0;
}
</style>
