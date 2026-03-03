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
            <el-radio-group v-model="filterType" @change="doSearch">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</el-radio-button>
            </el-radio-group>
          </div>
          <el-input
            v-model="keyword"
            placeholder="搜索标题..."
            prefix-icon="Search"
            @keyup.enter="doSearch"
            clearable
            @clear="doSearch"
            style="width: 260px"
          />
        </div>

        <!-- 标签筛选栏 -->
        <div class="filter-bar tag-filter-bar">
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

        <!-- 排序栏 -->
        <div class="sort-bar">
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import ContentCard from '@/components/ContentCard.vue'
import { contentApi } from '@/api/content'
import { tagApi } from '@/api/tag'

const route = useRoute()

const categories = ['小说', '散文', '诗词', '随笔', '名人名言', '杂谈']
const filterType = ref('')
const keyword = ref('')
const sortType = ref('')
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

// 标签筛选
const filterTagId = ref<number | null>(null)
const filterTagName = ref('')
const tagSearchValue = ref<number | null>(null)
const tagOptions = ref<any[]>([])

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

const applyQueryParams = () => {
  if (route.query.keyword) keyword.value = route.query.keyword as string
  if (route.query.type) filterType.value = route.query.type as string
  if (route.query.tagId) {
    filterTagId.value = Number(route.query.tagId)
    filterTagName.value = (route.query.tagName as string) || ''
  } else {
    filterTagId.value = null
    filterTagName.value = ''
  }
}

onMounted(async () => {
  // 预加载热门标签作为 el-select 的默认选项
  const res = await tagApi.hot(20)
  tagOptions.value = res.data
  applyQueryParams()
  loadList()
})

watch(() => route.query, () => {
  applyQueryParams()
  loadList()
})
</script>

<style scoped>
.category-page { min-height: calc(100vh + 1px); background: #f5f7fa; }
.page-content { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

.sticky-filters {
  position: sticky;
  top: 60px;
  z-index: 10;
  background: #f5f7fa;
  padding-top: 16px;
  padding-bottom: 8px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 10px 16px;
  border-radius: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
  gap: 10px;
}
.filter-section { display: flex; align-items: center; gap: 12px; }
.filter-label { font-size: 14px; color: #666; white-space: nowrap; }

.tag-filter-bar { justify-content: flex-start; }
.selected-tag-badge { font-size: 13px; }

.sort-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  padding: 8px 16px;
  border-radius: 8px;
  margin-bottom: 0;
}
.sort-label { font-size: 14px; color: #666; white-space: nowrap; }

.content-wrap { transition: opacity 0.2s; margin-top: 16px; }
.content-wrap.is-loading { opacity: 0.45; pointer-events: none; }

.content-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }
</style>
