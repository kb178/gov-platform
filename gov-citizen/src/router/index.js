import { createRouter, createWebHistory } from 'vue-router'

// 基础路由
const routes = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', layout: 'blank' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', layout: 'blank' }
  },
  {
    path: '/items',
    name: 'Items',
    component: () => import('@/views/items/index.vue'),
    meta: { title: '事项办理' }
  },
  {
    path: '/items/:id',
    name: 'ItemDetail',
    component: () => import('@/views/items/detail.vue'),
    meta: { title: '事项详情' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/index.vue'),
    meta: { title: '个人中心' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', layout: 'blank' }
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
    ? `${to.meta.title} - 海口政务服务平台`
    : '海口政务服务平台'

  // TODO: 测试阶段暂时关闭登录拦截，上线前恢复
  next()

  // // 登录鉴权
  // const token = localStorage.getItem('token')
  // const publicPaths = ['/login', '/register']
  //
  // if (!publicPaths.includes(to.path) && !token) {
  //   // 未登录访问需要登录的页面，跳转到登录页
  //   next('/login')
  // } else if (to.path === '/login' && token) {
  //   // 已登录访问登录页，跳转到首页
  //   next('/')
  // } else {
  //   next()
  // }
})

export default router
