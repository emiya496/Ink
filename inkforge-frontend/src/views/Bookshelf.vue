<template>
  <div class="bookshelf-page">
    <NavBar />
    <div class="page-content">
      <h2 class="page-title">我的书架</h2>
      <el-skeleton v-if="loading" :rows="5" animated />
      <div v-else>
        <el-alert
          v-if="hiddenTotal > 0"
          class="hidden-summary"
          type="warning"
          show-icon
          :closable="false"
        >
          <template #title>
            书架中有 {{ hiddenTotal }} 篇收藏当前不可见，已自动为你隐藏。
          </template>
          <div class="hidden-reason-tags">
            <el-tag
              v-for="item in hiddenReasonEntries"
              :key="item.reason"
              round
              type="warning"
              effect="plain"
            >
              {{ item.label }} {{ item.count }} 篇
            </el-tag>
          </div>
        </el-alert>

        <div class="content-grid">
          <div v-for="item in list" :key="item.id" class="card-wrapper">
            <ContentCard :content="item" />
            <el-button
              class="remove-btn"
              size="small"
              type="danger"
              plain
              @click.stop="removeFavorite(item.id)"
            >
              取消收藏
            </el-button>
          </div>
        </div>

        <el-empty
          v-if="list.length === 0"
          :description="hiddenTotal > 0 ? '当前可见收藏为空，已有不可见内容被自动隐藏。' : '书架还是空的，去收藏一些好作品吧。'"
        />

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadList"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import ContentCard from '@/components/ContentCard.vue'
import { favoriteApi } from '@/api/tag'

type HiddenReasonMap = Record<string, number>

const pageSize = 20
const hiddenReasonLabelMap: Record<string, string> = {
  deleted: '已删除',
  taken_down: '已下架',
  private: '私密内容',
  followers_only: '仅粉丝可见',
  other: '暂不可见',
}

const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const hiddenTotal = ref(0)
const hiddenByReason = ref<HiddenReasonMap>({})

const hiddenReasonEntries = computed(() =>
  Object.entries(hiddenByReason.value)
    .filter(([, count]) => Number(count) > 0)
    .map(([reason, count]) => ({
      reason,
      count: Number(count),
      label: hiddenReasonLabelMap[reason] || '暂不可见',
    })),
)

const loadList = async () => {
  loading.value = true
  try {
    const res = await favoriteApi.list({ page: page.value, size: pageSize })
    list.value = res.data.list || []
    total.value = res.data.total || 0
    hiddenTotal.value = res.data.hiddenTotal || 0
    hiddenByReason.value = res.data.hiddenByReason || {}
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (contentId: number) => {
  await favoriteApi.remove(contentId)
  ElMessage.success('已取消收藏')
  if (list.value.length === 1 && page.value > 1) {
    page.value -= 1
    await loadList()
    return
  }
  await loadList()
}

onMounted(loadList)
</script>

<style scoped>
.bookshelf-page {
  min-height: calc(100vh + 1px);
  background: #f0f2f5;
}

.page-content {
  max-width: 1200px;
  margin: 28px auto;
  padding: 0 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 22px;
  background: #409eff;
  border-radius: 2px;
}

.hidden-summary {
  margin-bottom: 18px;
}

.hidden-reason-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.card-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.remove-btn {
  width: 100%;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
  padding-bottom: 32px;
}
</style>
