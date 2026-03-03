<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { initTheme } from '@/composables/useTheme'

const userStore = useUserStore()

onMounted(async () => {
  initTheme()
  if (userStore.token) {
    await userStore.fetchUserInfo()
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background-color: #f5f5f5;
  color: #333;
}

/* ===== 暗夜模式全局覆盖 ===== */
html.dark body {
  background-color: #141414;
  color: #cfd3dc;
}

/* 页面背景 */
html.dark .home-page,
html.dark .category-page,
html.dark .bookshelf-page,
html.dark .user-center-page,
html.dark .detail-page,
html.dark .publish-page {
  background: #141414 !important;
}

/* 粘性筛选栏背景 */
html.dark .sticky-filters {
  background: #141414 !important;
}

/* 导航栏 */
html.dark .navbar {
  background: #1d1d2e !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4) !important;
}

/* 首页分类导航 */
html.dark .category-nav {
  background: #1d1d2e !important;
  border-bottom-color: #333 !important;
}

/* 白色卡片/面板统一变暗 */
html.dark .sort-bar,
html.dark .filter-bar,
html.dark .sidebar-card,
html.dark .editor-area,
html.dark .ai-panel,
html.dark .article-header,
html.dark .chapter-nav,
html.dark .article-body,
html.dark .comment-section,
html.dark .ai-reader-panel,
html.dark .user-profile-card,
html.dark .user-content,
html.dark .admin-page,
html.dark .stats-section,
html.dark .login-box,
html.dark .content-card {
  background: #1d1d2e !important;
  border-color: #333 !important;
}

html.dark .admin-header {
  background: #1d1d2e !important;
  border-bottom-color: #333 !important;
}

html.dark .admin-main {
  background: #141414 !important;
}

/* 列表项边框 */
html.dark .work-item {
  border-color: #333 !important;
}

/* 分割线 */
html.dark .edit-avatar,
html.dark .edit-email,
html.dark .edit-section,
html.dark .delete-account-section {
  border-top-color: #333 !important;
}

/* 文字颜色修正 */
html.dark .article-title { color: #e0e0e0 !important; }
html.dark .article-content { color: #ccc !important; }
html.dark .work-title { color: #cfd3dc !important; }
html.dark .page-title { color: #cfd3dc !important; }
html.dark .brand-name { color: #66b1ff !important; }
</style>
