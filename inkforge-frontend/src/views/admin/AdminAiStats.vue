<template>
  <div class="admin-page">
    <div class="stats-overview" v-if="stats">
      <el-card class="stat-card">
        <template #header><span>全站AI调用总次数</span></template>
        <div class="stat-number">{{ stats.total }}</div>
      </el-card>
    </div>

    <div class="stats-grid" v-if="stats">
      <!-- 按功能统计 -->
      <div class="stats-section">
        <h3 class="stats-section-title">按功能统计</h3>
        <el-table :data="functionStats" border>
          <el-table-column prop="name" label="功能" />
          <el-table-column prop="count" label="调用次数">
            <template #default="{ row }">
              <div class="count-bar-wrap">
                <span class="count-num">{{ row.count }}</span>
                <div class="count-bar" :style="{ width: getBarWidth(row.count, maxFuncCount) + '%' }" />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 按用户统计 -->
      <div class="stats-section">
        <h3 class="stats-section-title">用户调用排行（Top 20）</h3>
        <el-table :data="userStats" border>
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column prop="count" label="调用次数">
            <template #default="{ row }">
              <div class="count-bar-wrap">
                <span class="count-num">{{ row.count }}</span>
                <div class="count-bar" :style="{ width: getBarWidth(row.count, maxUserCount) + '%' }" />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-skeleton :rows="8" animated v-if="loading" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const stats = ref<any>(null)
const loading = ref(false)

const funcNameMap: Record<string, string> = {
  generate: 'AI续写', summary: '摘要生成', polish: '文章润色',
  keywords: '关键词提取', sentiment: '情感分析', style: '文风分析', qa: 'AI问答'
}

const functionStats = computed(() => {
  if (!stats.value?.byFunction) return []
  return Object.entries(stats.value.byFunction).map(([key, val]) => ({
    name: funcNameMap[key] || key,
    count: val
  })).sort((a: any, b: any) => b.count - a.count)
})

const userStats = computed(() => {
  if (!stats.value?.byUser) return []
  return Object.entries(stats.value.byUser).map(([userId, count]) => ({
    userId, count
  })).sort((a: any, b: any) => b.count - a.count).slice(0, 20)
})

const maxFuncCount = computed(() => Math.max(...functionStats.value.map((i: any) => i.count), 1))
const maxUserCount = computed(() => Math.max(...userStats.value.map((i: any) => i.count), 1))

const getBarWidth = (val: number, max: number) => Math.round((val / max) * 100)

const loadStats = async () => {
  loading.value = true
  try {
    const res = await adminApi.getAiStats()
    stats.value = res.data
  } finally { loading.value = false }
}

onMounted(loadStats)
</script>

<style scoped>
.admin-page { display: flex; flex-direction: column; gap: 20px; }
.stats-overview { display: flex; gap: 16px; }
.stat-card { flex: 1; }
.stat-number { font-size: 48px; font-weight: 700; color: #409eff; text-align: center; padding: 20px 0; }
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.stats-section { background: #fff; border-radius: 8px; padding: 20px; }
.stats-section-title { font-size: 16px; font-weight: 600; color: #333; margin-bottom: 16px; }
.count-bar-wrap { display: flex; align-items: center; gap: 10px; }
.count-num { width: 40px; text-align: right; font-weight: 600; color: #409eff; }
.count-bar { height: 16px; background: linear-gradient(90deg, #409eff, #67c23a); border-radius: 8px; min-width: 4px; transition: width 0.3s; }
</style>
