<template>
  <div class="category-page">
    <NavBar />
    <div class="page-content">
      <!-- 固定筛选区 -->
      <div class="sticky-filters">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <div class="filter-section">
            <span class="filter-label">分类：</span>
            <el-radio-group v-model="filterType" @change="onTypeChange">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</el-radio-button>
              <el-radio-button value="__user__">用户</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 标签筛选栏（仅内容模式显示） -->
        <div v-if="!isUserMode" class="filter-bar tag-filter-bar">
          <div class="filter-section" style="flex: 1; flex-wrap: wrap; gap: 8px;">
            <span class="filter-label">标签：</span>
            <el-tag
              v-if="filterTagId"
              closable
              type="primary"
              @close="clearTagFilter"
              class="selected-tag-badge"
            >#{{ filterTagName }}</el-tag>
            <el-select
              v-else
              v-model="tagSearchValue"
              filterable
              remote
              :remote-method="searchTags"
              placeholder="搜索或选择标签..."
              clearable
              style="width: 220px"
              @change="onTagSelect"
            >
              <el-option
                v-for="t in tagOptions"
                :key="t.id"
                :label="'#' + t.tagName"
                :value="t.id"
              />
            </el-select>
          </div>
        </div>

        <!-- 排序栏（仅内容模式显示） -->
        <div v-if="!isUserMode" class="sort-bar">
          <span class="sort-label">排序：</span>
          <el-radio-group v-model="sortType" @change="doSearch">
            <el-radio-button value="">最新发布</el-radio-button>
            <el-radio-button value="viewCount">总阅读</el-radio-button>
            <el-radio-button value="weeklyReads">周阅读</el-radio-button>
            <el-radio-button value="likeCount">点赞量</el-radio-button>
            <el-radio-button value="favorites">收藏量</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 内容列表 -->
      <template v-if="!isUserMode">
        <el-skeleton :rows="6" animated v-if="loading && list.length === 0" />
        <div :class="['content-wrap', { 'is-loading': loading && list.length > 0 }]">
          <div class="content-grid">
            <ContentCard v-for="item in list" :key="item.id" :content="item" />
          </div>
          <el-empty v-if="list.length === 0 && !loading" description="暂无内容" />
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="page"
              :page-size="20"
              :total="total"
              layout="prev, pager, next, total"
              @current-change="loadList"
            />
          </div>
        </div>
      </template>

      <!-- 用户列表 -->
      <template v-else>
        <el-skeleton :rows="4" animated v-if="userLoading && userList.length === 0" />
        <div :class="['content-wrap', { 'is-loading': userLoading && userList.length > 0 }]">
          <div class="user-grid">
            <div
              v-for="user in userList"
              :key="user.id"
              class="user-card"
              @click="router.push(`/user/${user.id}`)"
            >
              <el-avatar :src="user.avatar" :size="64" class="user-card-avatar">
                {{ user.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <div class="user-card-info">
                <div class="user-card-name">{{ user.username }}</div>
                <div class="user-card-bio">{{ user.bio || '这个人很懒，什么都没写~' }}</div>
                <div class="user-card-stats">
                  <span>粉丝 {{ user.followersCount }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-if="userList.length === 0 && !userLoading" description="暂无用户" />
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="userPage"
              :page-size="20"
              :total="userTotal"
              layout="prev, pager, next, total"
              @current-change="loadUsers"
            />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import ContentCard from '@/components/ContentCard.vue'
import { contentApi } from '@/api/content'
import { tagApi } from '@/api/tag'
import { userApi } from '@/api/user'

const route = useRoute()
const router = useRouter()

const categories = ['小说', '散文', '诗词', '随笔', '名人名言', '杂谈']
const filterType = ref('')
const sortType = ref('')
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

// 是否用户模式
const isUserMode = computed(() => filterType.value === '__user__')

// 标签筛选
const filterTagId = ref<number | null>(null)
const filterTagName = ref('')
const tagSearchValue = ref<number | null>(null)
const tagOptions = ref<any[]>([])

// 用户搜索
const userList = ref<any[]>([])
const userTotal = ref(0)
const userPage = ref(1)
const userLoading = ref(false)

// 当前搜索关键词（从 URL 读取，不再有自己的 input）
const keyword = computed(() => (route.query.keyword as string) || '')

const searchTags = async (query: string) => {
  if (!query) {
    const res = await tagApi.hot(20)
    tagOptions.value = res.data
    return
  }
  const res = await tagApi.list()
  tagOptions.value = res.data.filter((t: any) =>
    t.tagName.toLowerCase().includes(query.toLowerCase())
  )
}

const onTagSelect = (id: number | null) => {
  if (!id) return
  const tag = tagOptions.value.find(t => t.id === id)
  if (tag) {
    filterTagId.value = tag.id
    filterTagName.value = tag.tagName
  }
  tagSearchValue.value = null
  doSearch()
}

const clearTagFilter = () => {
  filterTagId.value = null
  filterTagName.value = ''
  doSearch()
}

const onTypeChange = () => {
  page.value = 1
  userPage.value = 1
  if (isUserMode.value) {
    loadUsers()
  } else {
    loadList()
  }
}

const doSearch = () => {
  page.value = 1
  loadList()
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await contentApi.list({
      page: page.value,
      size: 20,
      type: filterType.value || undefined,
      keyword: keyword.value || undefined,
      sortBy: sortType.value || undefined,
      tagId: filterTagId.value || undefined,
    })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  userLoading.value = true
  try {
    const res = await userApi.searchUsers(keyword.value, userPage.value, 20)
    userList.value = res.data.list
    userTotal.value = res.data.total
  } finally {
    userLoading.value = false
  }
}

const applyQueryParams = () => {
  // keyword 来自 URL，computed 自动处理
  if (route.query.type) filterType.value = route.query.type as string
  if (route.query.tab === 'user') filterType.value = '__user__'
  if (route.query.tagId) {
    filterTagId.value = Number(route.query.tagId)
    filterTagName.value = (route.query.tagName as string) || ''
  } else {
    filterTagId.value = null
    filterTagName.value = ''
  }
}

onMounted(async () => {
  const res = await tagApi.hot(20)
  tagOptions.value = res.data
  applyQueryParams()
  if (isUserMode.value) {
    loadUsers()
  } else {
    loadList()
  }
})

watch(() => route.query, () => {
  applyQueryParams()
  if (isUserMode.value) {
    loadUsers()
  } else {
    loadList()
  }
})
</script>

<style scoped>
.category-page { min-height: calc(100vh + 1px); background: #f0f2f5; }
.page-content { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

.sticky-filters {
  position: sticky;
  top: 60px;
  z-index: 10;
  background: #f0f2f5;
  padding-top: 16px;
  padding-bottom: 8px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.filter-section { display: flex; align-items: center; gap: 12px; }
.filter-label { font-size: 14px; color: #888; white-space: nowrap; font-weight: 500; }

.tag-filter-bar { justify-content: flex-start; }
.selected-tag-badge { font-size: 13px; }

.sort-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 8px 16px;
  border-radius: 12px;
  margin-bottom: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.sort-label { font-size: 14px; color: #888; white-space: nowrap; font-weight: 500; }

.content-wrap { transition: opacity 0.2s; margin-top: 16px; }
.content-wrap.is-loading { opacity: 0.45; pointer-events: none; }

.content-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

/* 用户卡片网格 */
.user-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.user-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  text-align: center;
}
.user-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: rgba(64, 158, 255, 0.15);
}
.user-card-avatar { flex-shrink: 0; }
.user-card-info { width: 100%; }
.user-card-name { font-size: 15px; font-weight: 700; color: #1a1a1a; margin-bottom: 4px; }
.user-card-bio {
  font-size: 12px;
  color: #999;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 6px;
}
.user-card-stats { font-size: 12px; color: #bbb; }

.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; padding-bottom: 32px; }
</style>
