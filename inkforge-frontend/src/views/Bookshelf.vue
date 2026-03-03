<template>
  <div class="bookshelf-page">
    <NavBar />
    <div class="page-content">
      <h2 class="page-title">我的书架</h2>
      <el-skeleton :rows="5" animated v-if="loading" />
      <div v-else>
        <div class="content-grid">
          <ContentCard v-for="item in list" :key="item.id" :content="item" />
        </div>
        <el-empty v-if="list.length === 0" description="书架还是空的，去收藏一些好作品吧" />
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            :page-size="20"
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
import { ref, onMounted } from 'vue'
import NavBar from '@/components/NavBar.vue'
import ContentCard from '@/components/ContentCard.vue'
import { favoriteApi } from '@/api/tag'

const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const res = await favoriteApi.list({ page: page.value, size: 20 })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.bookshelf-page { min-height: calc(100vh + 1px); background: #f5f7fa; }
.page-content { max-width: 1200px; margin: 24px auto; padding: 0 20px; }
.page-title { font-size: 24px; font-weight: 700; color: #333; margin-bottom: 20px; }
.content-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }
</style>
