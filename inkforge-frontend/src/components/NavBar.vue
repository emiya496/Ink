<template>
  <el-header class="navbar">
    <!-- 返回按钮：绝对定位在导航栏左侧，不影响内部 flex 布局 -->
    <el-button
      v-if="route.path !== '/'"
      class="back-btn"
      :icon="ArrowLeft"
      circle
      @click="router.back()"
    />

    <div class="navbar-inner">
      <!-- Logo 最左 -->
      <div class="navbar-brand" @click="router.push('/')">
        <img src="/logo.png" class="brand-logo-img" alt="InkForge" />
      </div>

      <!-- 导航菜单 -->
      <el-menu mode="horizontal" :ellipsis="false" class="navbar-menu" :default-active="activeMenu">
        <el-menu-item index="/" @click="router.push('/')">首页</el-menu-item>
        <el-menu-item index="/category" @click="router.push('/category')">分类</el-menu-item>
        <el-menu-item v-if="userStore.isLoggedIn()" index="/publish" @click="router.push('/publish')">创作中心</el-menu-item>
        <el-menu-item v-if="userStore.isLoggedIn()" index="/bookshelf" @click="router.push('/bookshelf')">我的书架</el-menu-item>
        <el-menu-item v-if="userStore.isAdmin()" index="/admin" @click="router.push('/admin')" class="admin-entry">管理入口</el-menu-item>
      </el-menu>

      <!-- 搜索框（书架后面，撑开剩余空间） -->
      <el-input
        v-model="searchKeyword"
        placeholder="搜索内容..."
        prefix-icon="Search"
        @keyup.enter="doSearch"
        class="search-input"
        clearable
      />

      <!-- 最右：登录/注册 或 头像 -->
      <div class="navbar-right">
        <el-button :icon="isDark ? 'Sunny' : 'Moon'" circle class="theme-btn" @click="toggleTheme" />
        <template v-if="!userStore.isLoggedIn()">
          <el-button @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/login?tab=register')">注册</el-button>
        </template>
        <template v-else>
          <el-dropdown @command="handleCommand">
            <div class="user-avatar">
              <el-avatar :src="userStore.userInfo?.avatar" :size="36">
                {{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="my-content">我的作品</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { isDark, toggleTheme } from '@/composables/useTheme'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')

// 在分类页时，搜索框与 URL keyword 参数保持同步
watch(() => route.query.keyword, (val) => {
  searchKeyword.value = (val as string) || ''
}, { immediate: true })

// 根据当前路径计算激活菜单项
const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/publish')) return '/publish'
  if (path.startsWith('/category')) return '/category'
  if (path.startsWith('/bookshelf')) return '/bookshelf'
  if (path.startsWith('/admin')) return '/admin'
  if (path === '/') return '/'
  return ''
})

const doSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/category', query: { keyword: searchKeyword.value.trim() } })
  }
}

const handleCommand = async (cmd: string) => {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } else if (cmd === 'profile') {
    router.push('/user')
  } else if (cmd === 'my-content') {
    router.push('/user?tab=content')
  }
}
</script>

<style scoped>
.navbar {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.06), 0 4px 16px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px;
  padding: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.8);
  transition: background 0.3s, box-shadow 0.3s;
}
.navbar-inner {
  max-width: 1300px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 0;
}
.back-btn {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  border: none !important;
  background: #f5f7fa !important;
  color: #555 !important;
  transition: background 0.2s, color 0.2s !important;
}
.back-btn:hover {
  background: #e9ecf0 !important;
  color: #409eff !important;
}
.navbar-brand {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex-shrink: 0;
  margin-right: 8px;
  transition: opacity 0.2s;
}
.navbar-brand:hover { opacity: 0.8; }
.brand-logo-img {
  height: 40px;
  width: auto;
  display: block;
}
.navbar-menu {
  border-bottom: none !important;
  flex-shrink: 0;
  background: transparent !important;
}
:deep(.navbar-menu .el-menu-item) {
  font-size: 15px;
  color: #555 !important;
  border-bottom: 2px solid transparent !important;
  transition: color 0.2s, border-color 0.2s !important;
}
:deep(.navbar-menu .el-menu-item:hover),
:deep(.navbar-menu .el-menu-item.is-active) {
  color: #409eff !important;
  background: transparent !important;
  border-bottom-color: #409eff !important;
}
.search-input {
  flex: 1;
  min-width: 120px;
  margin: 0 16px;
}
:deep(.search-input .el-input__wrapper) {
  border-radius: 20px;
  background: #f5f7fa;
  box-shadow: none !important;
  border: 1px solid transparent;
  transition: border-color 0.2s, background 0.2s;
}
:deep(.search-input .el-input__wrapper:hover),
:deep(.search-input .el-input__wrapper.is-focus) {
  background: #fff;
  border-color: #409eff;
}
.navbar-right {
  width: 320px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 4px;
  border-radius: 20px;
  transition: background 0.2s;
}
.user-avatar:hover { background: #f5f7fa; }
.username { font-size: 14px; color: #333; font-weight: 500; }
.admin-entry { color: #e6a23c !important; font-weight: bold; }
.theme-btn {
  border: none !important;
  background: #f5f7fa !important;
  color: #555 !important;
  transition: background 0.2s, color 0.2s !important;
}
.theme-btn:hover {
  background: #e9ecf0 !important;
  color: #409eff !important;
}
</style>

<style>
/* 导航栏暗黑模式 */
html.dark .navbar-menu .el-menu-item {
  color: #cfd3dc !important;
  background: transparent !important;
}
html.dark .navbar-menu .el-menu-item:hover,
html.dark .navbar-menu .el-menu-item.is-active {
  color: #66b1ff !important;
  background: transparent !important;
}
html.dark .navbar-menu,
html.dark .el-menu--horizontal { background: transparent !important; }
html.dark .back-btn { background: #252535 !important; color: #aaa !important; }
html.dark .back-btn:hover { background: #333350 !important; color: #66b1ff !important; }
html.dark .theme-btn { background: #252535 !important; color: #aaa !important; }
html.dark .theme-btn:hover { background: #333350 !important; color: #66b1ff !important; }
html.dark .user-avatar:hover { background: #252535 !important; }
html.dark .user-avatar .username { color: #cfd3dc !important; }
html.dark .search-input .el-input__wrapper { background: #252535 !important; }
html.dark .search-input .el-input__wrapper:hover,
html.dark .search-input .el-input__wrapper.is-focus {
  background: #2a2a40 !important;
  border-color: #409eff !important;
}
</style>
