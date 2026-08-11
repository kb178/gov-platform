import { createRouter, createWebHistory } from 'vue-router'

// 基础路由
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      }
    ]
  },
  {
    path: '/approval',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/approval/pending',
    children: [
      {
        path: 'pending',
        name: 'ApprovalPending',
        component: () => import('@/views/approval/pending.vue'),
        meta: { title: '待我审批', icon: 'Tickets' }
      },
      {
        path: 'done',
        name: 'ApprovalDone',
        component: () => import('@/views/approval/done.vue'),
        meta: { title: '已办事项', icon: 'Finished' }
      },
      {
        path: 'monitor',
        name: 'ApprovalMonitor',
        component: () => import('@/views/approval/monitor.vue'),
        meta: { title: '流程监控', icon: 'View' }
      }
    ]
  },
  {
    path: '/item',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/item/list',
    children: [
      {
        path: 'category',
        name: 'ItemCategory',
        component: () => import('@/views/item/category.vue'),
        meta: { title: '事项分类', icon: 'Folder' }
      },
      {
        path: 'list',
        name: 'ItemList',
        component: () => import('@/views/item/list.vue'),
        meta: { title: '事项列表', icon: 'Document' }
      },
      {
        path: 'guide',
        name: 'ItemGuide',
        component: () => import('@/views/item/guide.vue'),
        meta: { title: '办事指南', icon: 'Notebook' }
      },
      {
        path: 'template',
        name: 'ItemTemplate',
        component: () => import('@/views/item/template.vue'),
        meta: { title: '表单模板', icon: 'Files' }
      }
    ]
  },
  {
    path: '/license',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/license/template',
    children: [
      {
        path: 'template',
        name: 'LicenseTemplate',
        component: () => import('@/views/license/template.vue'),
        meta: { title: '证照模板', icon: 'Stamp' }
      },
      {
        path: 'list',
        name: 'LicenseList',
        component: () => import('@/views/license/list.vue'),
        meta: { title: '证照列表', icon: 'Ticket' }
      }
    ]
  },
  {
    path: '/statistics',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/statistics/overview',
    children: [
      {
        path: 'overview',
        name: 'StatisticsOverview',
        component: () => import('@/views/statistics/overview.vue'),
        meta: { title: '办件统计', icon: 'DataAnalysis' }
      },
      {
        path: 'screen',
        name: 'StatisticsScreen',
        component: () => import('@/views/statistics/screen.vue'),
        meta: { title: '数据大屏', icon: 'Monitor' }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/system/user',
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/user.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('@/views/system/role.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'dept',
        name: 'SystemDept',
        component: () => import('@/views/system/dept.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'dict',
        name: 'SystemDict',
        component: () => import('@/views/system/dict.vue'),
        meta: { title: '字典管理', icon: 'Collection' }
      },
      {
        path: 'log',
        name: 'SystemLog',
        component: () => import('@/views/system/log.vue'),
        meta: { title: '操作日志', icon: 'Document' }
      },
      {
        path: 'config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config.vue'),
        meta: { title: '系统参数', icon: 'Setting' }
      }
    ]
  },
  {
    path: '/message',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/message/notice',
    children: [
      {
        path: 'notice',
        name: 'MessageNotice',
        component: () => import('@/views/message/notice.vue'),
        meta: { title: '系统公告', icon: 'Bell' }
      },
      {
        path: 'list',
        name: 'MessageList',
        component: () => import('@/views/message/list.vue'),
        meta: { title: '站内消息', icon: 'ChatDotRound' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title
    ? `${to.meta.title} - 海口政务管理后台`
    : '海口政务管理后台'

  const token = localStorage.getItem('admin_token')

  // 已登录
  if (token) {
    if (to.path === '/login') {
      // 已登录跳登录页，重定向到首页
      next('/dashboard')
    } else {
      next()
    }
  } else {
    // 未登录
    if (to.path === '/login') {
      next()
    } else {
      // 未登录访问其他页，跳转登录并记录来源
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }
})

export default router
