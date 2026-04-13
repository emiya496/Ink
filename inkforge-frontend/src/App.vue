<template>
  <router-view v-slot="{ Component }">
    <keep-alive :include="['Home']">
      <component :is="Component" />
    </keep-alive>
  </router-view>
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

/* 作品卡片暗色修正 */
html.dark .card-title { color: #cfd3dc !important; }
html.dark .card-author { color: #888 !important; }
html.dark .card-stats { color: #666 !important; }

/* 排行榜 & 侧边栏暗色修正 */
html.dark .rank-title { color: #cfd3dc !important; }
html.dark .rank-item:hover { background: #252535 !important; }
html.dark .sidebar-title { color: #cfd3dc !important; }

/* 排序栏 & 分类导航暗色修正 */
html.dark .sort-btn { color: #aaa !important; }
html.dark .sort-btn.active,
html.dark .sort-btn:hover { color: #66b1ff !important; background: #1a2a3a !important; }
html.dark .more-btn { color: #66b1ff !important; }
html.dark .cat-item { color: #aaa !important; }
html.dark .cat-item.active,
html.dark .cat-item:hover { color: #66b1ff !important; }

/* 评论区暗色修正 */
html.dark .comment-author { color: #cfd3dc !important; }
html.dark .comment-text { color: #aaa !important; }
html.dark .section-title { color: #cfd3dc !important; }

/* 作者名暗色修正 */
html.dark .author-name { color: #aaa !important; }

/* 封面占位背景暗色修正 */
html.dark .cover-placeholder { background: linear-gradient(135deg, #1d2b3a 0%, #2a3a50 100%) !important; }
html.dark .cover-placeholder.type-success { background: linear-gradient(135deg, #1a2e1a 0%, #2a4a2a 100%) !important; }
html.dark .cover-placeholder.type-warning { background: linear-gradient(135deg, #2e2a10 0%, #4a4010 100%) !important; }
html.dark .cover-placeholder.type-danger { background: linear-gradient(135deg, #2e1a1a 0%, #4a2a2a 100%) !important; }
html.dark .cover-placeholder.type-info { background: linear-gradient(135deg, #1a1a2e 0%, #2a2a4a 100%) !important; }

/* ---- 小说简介 ---- */
html.dark .novel-synopsis { background: #1a2030 !important; border-color: #2a3a50 !important; }
html.dark .synopsis-text { color: #aaa !important; }

/* ---- 个人中心：作品/草稿/点赞列表格子 ---- */
html.dark .work-item { background: #212132 !important; }
html.dark .work-item:hover { background: #252540 !important; border-color: rgba(64,158,255,0.3) !important; }
html.dark .work-meta { color: #888 !important; }
html.dark .work-time,
html.dark .work-stat { color: #666 !important; }

/* ---- 个人中心：关注/粉丝列表格子 ---- */
html.dark .user-item { background: #212132 !important; border-color: #333 !important; }
html.dark .user-item:hover { background: #252540 !important; }
html.dark .user-item-name { color: #cfd3dc !important; }
html.dark .user-item-bio { color: #777 !important; }

/* ---- 个人中心：左侧用户信息 ---- */
html.dark .username { color: #cfd3dc !important; }

/* ---- 发布页 ---- */
html.dark .publish-header h2 { color: #cfd3dc !important; }
html.dark .editor-container { border-color: #444 !important; }
html.dark .chapter-item { background: #212132 !important; border-color: #333 !important; }
html.dark .chapter-item:hover { border-color: rgba(64,158,255,0.4) !important; }
html.dark .chapter-item-gap { background: #252015 !important; border-color: rgba(180,130,60,0.3) !important; }
html.dark .chapter-index { color: #666 !important; }
html.dark .chapter-title-text { color: #cfd3dc !important; }
html.dark .ai-result-header { color: #cfd3dc !important; }
html.dark .ai-result-content { background: #1a2030 !important; color: #aaa !important; border-color: #2a3a50 !important; }
html.dark .ai-panel-header { border-bottom-color: #333 !important; }
html.dark .cover-add { border-color: #444 !important; color: #666 !important; }
html.dark .cover-add:hover { border-color: #409eff !important; color: #66b1ff !important; background: #1a2535 !important; }
html.dark .cover-thumb { border-color: #444 !important; }
html.dark .undo-bar { background: #2a2010 !important; border-color: #4a3a10 !important; color: #c8a040 !important; }

/* ---- wangEditor 编辑器暗色适配 ---- */
html.dark .w-e-toolbar { background-color: #252535 !important; border-color: #444 !important; }
html.dark .w-e-toolbar .w-e-bar-item button { color: #cfd3dc !important; }
html.dark .w-e-toolbar .w-e-bar-item button:hover { background: #333350 !important; }
html.dark .w-e-text-container { background-color: #1d1d2e !important; border-color: #444 !important; }
html.dark .w-e-text-container [data-slate-editor] { color: #cfd3dc !important; }
html.dark .w-e-text-placeholder { color: #555 !important; }

/* ---- 管理后台暗色修正 ---- */
html.dark .admin-title { color: #cfd3dc !important; }
html.dark .admin-user { color: #aaa !important; }
html.dark .preview-info { color: #888 !important; }
html.dark .preview-body { color: #aaa !important; }
html.dark .stats-section-title { color: #cfd3dc !important; }
</style>
