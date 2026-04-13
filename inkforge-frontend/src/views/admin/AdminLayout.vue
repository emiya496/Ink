<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">
        <img src="/logo.png" class="admin-logo-img" alt="InkForge" />
        <span class="admin-logo-text">管理后台</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="admin-menu"
        background-color="#001529"
        text-color="#ffffffa0"
        active-text-color="#fff"
      >
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon><span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/contents">
          <el-icon><Document /></el-icon><span>内容管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><Collection /></el-icon><span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatDotRound /></el-icon><span>评论管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/ai-stats">
          <el-icon><DataLine /></el-icon><span>AI 统计</span>
        </el-menu-item>
      </el-menu>
      <div class="admin-back" @click="router.push('/')">← 返回前台</div>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <span class="admin-title">{{ pageTitle }}</span>
        <div class="admin-user">
          <el-button :icon="isDark ? 'Sunny' : 'Moon'" circle @click="toggleTheme" />
          <el-avatar :size="32">{{ userStore.userInfo?.username?.charAt(0) }}</el-avatar>
          <span>{{ userStore.userInfo?.username }}</span>
          <el-button text @click="userStore.logout(); router.push('/')">退出</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { isDark, toggleTheme } from '@/composables/useTheme'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const titleMap: Record<string, string> = {
  '/admin/users': '用户管理',
  '/admin/contents': '内容管理',
  '/admin/tags': '标签管理',
  '/admin/comments': '评论管理',
  '/admin/ai-stats': 'AI调用统计',
}
const pageTitle = computed(() => titleMap[route.path] || '管理后台')
</script>

<style scoped>
.admin-layout { height: 100vh; }
.admin-aside {
  background: linear-gradient(180deg, #0f1f3d 0%, #001529 100%);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.2);
}
.admin-logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 16px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.admin-logo-img {
  width: 140px;
  height: auto;
  filter: brightness(1.1);
}
.admin-logo-text {
  color: rgba(255, 255, 255, 0.45);
  font-size: 11px;
  margin-top: 6px;
  letter-spacing: 3px;
  text-transform: uppercase;
}
.admin-menu { border-right: none; flex: 1; background: transparent !important; }
:deep(.admin-menu .el-menu-item) {
  margin: 2px 8px;
  border-radius: 8px;
  transition: background 0.2s !important;
}
:deep(.admin-menu .el-menu-item.is-active) {
  background: rgba(64, 158, 255, 0.2) !important;
}
.admin-back {
  color: rgba(255, 255, 255, 0.4);
  font-size: 13px;
  padding: 14px 16px;
  cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  transition: color 0.2s, background 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}
.admin-back:hover { color: #fff; background: rgba(255,255,255,0.06); }
.admin-header {
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.admin-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}
.admin-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 18px;
  background: #409eff;
  border-radius: 2px;
}
.admin-user { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #555; }
.admin-main { background: #f0f2f5; padding: 24px; overflow: auto; }
</style>
