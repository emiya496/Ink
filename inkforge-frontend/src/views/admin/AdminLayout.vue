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
  background: #001529;
  display: flex;
  flex-direction: column;
}
.admin-logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ffffff20;
}
.admin-logo-img {
  width: 140px;
  height: auto;
}
.admin-logo-text {
  color: #ffffff80;
  font-size: 12px;
  margin-top: 4px;
  letter-spacing: 2px;
}
.admin-menu { border-right: none; flex: 1; }
.admin-back {
  color: #ffffff60;
  font-size: 13px;
  padding: 16px;
  cursor: pointer;
  border-top: 1px solid #ffffff20;
}
.admin-back:hover { color: #fff; }
.admin-header {
  background: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.admin-title { font-size: 18px; font-weight: 600; color: #333; }
.admin-user { display: flex; align-items: center; gap: 10px; }
.admin-main { background: #f5f7fa; padding: 24px; }
</style>
