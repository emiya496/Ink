import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/category',
    component: () => import('@/views/Category.vue')
  },
  {
    path: '/publish',
    component: () => import('@/views/Publish.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/publish/:id',
    component: () => import('@/views/Publish.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/detail/:id',
    component: () => import('@/views/Detail.vue')
  },
  {
    path: '/user',
    component: () => import('@/views/UserCenter.vue'),
    meta: { requireAuth: true }
  },
  {
    path: '/bookshelf',
    component: () => import('@/views/Bookshelf.vue'),
    meta: { requireAuth: true }
  },
  // 管理后台
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      { path: '', redirect: '/admin/users' },
      { path: 'users', component: () => import('@/views/admin/AdminUsers.vue') },
      { path: 'contents', component: () => import('@/views/admin/AdminContents.vue') },
      { path: 'tags', component: () => import('@/views/admin/AdminTags.vue') },
      { path: 'comments', component: () => import('@/views/admin/AdminComments.vue') },
      { path: 'ai-stats', component: () => import('@/views/admin/AdminAiStats.vue') },
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requireAuth && !userStore.isLoggedIn()) {
    next('/login')
    return
  }

  if (to.meta.requireAdmin) {
    // 确保用户信息已加载
    if (!userStore.userInfo) {
      await userStore.fetchUserInfo()
    }
    if (!userStore.isAdmin()) {
      next('/')
      return
    }
  }

  next()
})

export default router
