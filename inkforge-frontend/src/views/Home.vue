<template>
  <div class="home-page">
    <NavBar />

    <!-- 分类导航 -->
    <div class="category-nav">
      <div class="category-inner">
        <span
          v-for="cat in categories"
          :key="cat.value"
          :class="['cat-item', { active: activeType === cat.value }]"
          @click="selectType(cat.value)"
        >{{ cat.label }}</span>
      </div>
    </div>

    <div class="main-layout">
      <!-- 左侧主区域 -->
      <div class="content-area">

        <!-- 热门横幅 -->
        <div
          v-if="hotBanner"
          class="hero-banner"
          @click="router.push(`/detail/${hotBanner.id}`)"
          :style="hotBanner.coverImage ? { backgroundImage: `url(${hotBanner.coverImage})` } : {}"
        >
          <div class="banner-overlay">
            <el-tag size="small" type="warning" class="banner-tag">本周最火</el-tag>
            <h2 class="banner-title">{{ hotBanner.title }}</h2>
            <div class="banner-meta">
              <span>{{ hotBanner.type }}</span>
              <span>· {{ hotBanner.username }}</span>
              <span>· <el-icon><View /></el-icon> {{ hotBanner.viewCount }}</span>
            </div>
          </div>
        </div>

        <!-- 排序栏 + 查看更多 -->
        <div class="sort-bar">
          <div class="sort-left">
            <span
              :class="['sort-btn', { active: sortType === 'latest' }]"
              @click="sortType = 'latest'; loadContent()"
            >最新发布</span>
            <span
              :class="['sort-btn', { active: sortType === 'hot' }]"
              @click="sortType = 'hot'; loadContent()"
            >热门内容</span>
          </div>
          <span class="more-btn" @click="goMore">查看更多 →</span>
        </div>

        <!-- 作品网格 5列 -->
        <el-skeleton :rows="3" animated v-if="loading && contentList.length === 0" />
        <div :class="['content-wrap', { 'is-loading': loading && contentList.length > 0 }]">
          <div class="content-grid">
            <ContentCard
              v-for="item in contentList"
              :key="item.id"
              :content="item"
            />
          </div>
          <el-empty v-if="contentList.length === 0 && !loading" description="暂无内容" />
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="sidebar">
        <!-- 热门标签 -->
        <div class="sidebar-card">
          <h3 class="sidebar-title">热门标签</h3>
          <div class="tag-cloud">
            <el-tag
              v-for="tag in hotTags"
              :key="tag.id"
              effect="plain"
              class="cloud-tag"
              @click="searchByTag(tag)"
            >#{{ tag.tagName }} <span class="tag-count">{{ formatCount(tag.useCount) }}</span></el-tag>
          </div>
        </div>

        <!-- 排行榜 -->
        <div class="sidebar-card">
          <h3 class="sidebar-title">排行榜</h3>
          <el-tabs v-model="rankTab" @tab-change="loadRanking" size="small" class="rank-tabs">
            <el-tab-pane label="总阅读" name="reads" />
            <el-tab-pane label="周阅读" name="weeklyReads" />
            <el-tab-pane label="点赞" name="likes" />
            <el-tab-pane label="收藏" name="favorites" />
          </el-tabs>
          <el-skeleton :rows="4" animated v-if="rankLoading" />
          <div v-else class="rank-list">
            <div
              v-for="(item, idx) in rankList"
              :key="item.id"
              class="rank-item"
              @click="router.push(`/detail/${item.id}`)"
            >
              <span :class="['rank-num', idx === 0 ? 'top1' : idx === 1 ? 'top2' : idx === 2 ? 'top3' : '']">{{ idx + 1 }}</span>
              <div class="rank-info">
                <span class="rank-title">{{ item.title }}</span>
                <span class="rank-stat">
                  <template v-if="rankTab === 'reads'">{{ item.viewCount }} 阅读</template>
                  <template v-else-if="rankTab === 'weeklyReads'">本周热读</template>
                  <template v-else-if="rankTab === 'likes'">{{ item.likeCount }} 点赞</template>
                  <template v-else>{{ item.favoriteCount }} 收藏</template>
                </span>
              </div>
            </div>
            <el-empty v-if="rankList.length === 0" description="暂无数据" :image-size="60" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { View } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import ContentCard from '@/components/ContentCard.vue'
import { contentApi } from '@/api/content'
import { tagApi } from '@/api/tag'

defineOptions({ name: 'Home' })

const router = useRouter()

const categories = [
  { label: '全部', value: '' },
  { label: '小说', value: '小说' },
  { label: '散文', value: '散文' },
  { label: '诗词', value: '诗词' },
  { label: '随笔', value: '随笔' },
  { label: '名人名言', value: '名人名言' },
  { label: '杂谈', value: '杂谈' },
]

const activeType = ref('')
const sortType = ref('latest')
const contentList = ref<any[]>([])
const hotTags = ref<any[]>([])
const hotBanner = ref<any>(null)
const loading = ref(false)

// 排行榜
const rankTab = ref('reads')
const rankList = ref<any[]>([])
const rankLoading = ref(false)

const selectType = (type: string) => {
  activeType.value = type
  loadContent()
  loadHotBanner()
  loadRanking()
}

const loadContent = async () => {
  loading.value = true
  try {
    const params: any = { page: 1, size: 10, type: activeType.value || undefined }
    if (sortType.value === 'hot') {
      // 热门排序：按浏览量降序，复用 list 接口（后端已按 createTime 排序，
      // 热门模式我们用 rank 接口的 reads 类型替代）
      const res = await contentApi.rank({ type: activeType.value || '', rankType: 'reads' })
      contentList.value = res.data
    } else {
      const res = await contentApi.list(params)
      contentList.value = res.data.list
    }
  } finally {
    loading.value = false
  }
}

const loadHotBanner = async () => {
  try {
    const res = await contentApi.hotBanner({ type: activeType.value || '' })
    hotBanner.value = res.data
  } catch {
    hotBanner.value = null
  }
}

const loadRanking = async () => {
  rankLoading.value = true
  try {
    const res = await contentApi.rank({ type: activeType.value || '', rankType: rankTab.value })
    rankList.value = res.data
  } finally {
    rankLoading.value = false
  }
}

const loadHotTags = async () => {
  const res = await tagApi.hot(21)
  hotTags.value = res.data
}

const formatCount = (n: number): string => {
  if (n >= 100000) return '10w+'
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

const searchByTag = (tag: any) => {
  router.push({ path: '/category', query: { tagId: tag.id, tagName: tag.tagName } })
}

const goMore = () => {
  if (activeType.value) {
    router.push({ path: '/category', query: { type: activeType.value } })
  } else {
    router.push('/category')
  }
}

onMounted(() => {
  loadContent()
  loadHotBanner()
  loadRanking()
  loadHotTags()
})
</script>

<style scoped>
.home-page { min-height: calc(100vh + 1px); background: #f0f2f5; }

/* 分类导航 */
.category-nav {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 60px;
  z-index: 10;
}
.category-inner {
  max-width: 1300px;
  margin: 0 auto;
  padding: 0 20px 0 68px;
  display: flex;
}
.cat-item {
  padding: 12px 20px;
  cursor: pointer;
  font-size: 15px;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
  white-space: nowrap;
}
.cat-item:hover { color: #409eff; }
.cat-item.active { color: #409eff; border-bottom-color: #409eff; font-weight: 600; }

/* 主体布局 */
.main-layout {
  max-width: 1300px;
  margin: 20px auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

/* 热门横幅 */
.hero-banner {
  height: 240px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
  background-size: cover;
  background-position: center;
  cursor: pointer;
  overflow: hidden;
  margin-bottom: 16px;
  transition: box-shadow 0.3s, transform 0.3s;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}
.hero-banner:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.22);
  transform: translateY(-2px);
}
.banner-overlay {
  width: 100%;
  height: 100%;
  padding: 24px 28px;
  background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.15) 100%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}
.banner-tag { margin-bottom: 8px; align-self: flex-start; }
.banner-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
}
.banner-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: rgba(255,255,255,0.75);
}
.banner-meta .el-icon { font-size: 13px; }

/* 排序栏 */
.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 10px 16px;
  border-radius: 10px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.sort-left { display: flex; gap: 8px; }
.sort-btn {
  cursor: pointer;
  font-size: 14px;
  color: #666;
  padding: 5px 12px;
  border-radius: 20px;
  transition: all 0.2s;
  font-weight: 500;
}
.sort-btn:hover { color: #409eff; background: #f0f7ff; }
.sort-btn.active { color: #409eff; background: #e8f3ff; font-weight: 600; }
.more-btn {
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background 0.2s;
}
.more-btn:hover { background: #f0f7ff; }

/* 5列内容网格 */
.content-wrap { transition: opacity 0.2s; }
.content-wrap.is-loading { opacity: 0.45; pointer-events: none; }
.content-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

/* 侧边栏 */
.sidebar { position: sticky; top: 100px; }
.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.sidebar-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 14px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
  display: flex;
  align-items: center;
  gap: 6px;
}
.sidebar-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 16px;
  background: #409eff;
  border-radius: 2px;
}

/* 热门标签 */
.tag-cloud {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px 6px;
}
.cloud-tag {
  cursor: pointer;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  overflow: hidden;
  box-sizing: border-box;
  transition: transform 0.15s;
}
.cloud-tag:hover { transform: scale(1.04); }
.tag-count { font-size: 11px; color: #999; margin-left: 4px; flex-shrink: 0; }

/* 排行榜 */
.rank-tabs { margin-bottom: 8px; }
.rank-list { display: flex; flex-direction: column; gap: 6px; }
.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 4px;
  border-radius: 8px;
  transition: background 0.15s;
}
.rank-item:hover { background: #f5f7fa; }
.rank-item:hover .rank-title { color: #409eff; }
.rank-num {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: #eee;
  color: #888;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.rank-num.top1 { background: linear-gradient(135deg, #f7b733, #fc4a1a); color: #fff; }
.rank-num.top2 { background: linear-gradient(135deg, #bdc3c7, #95a5a6); color: #fff; }
.rank-num.top3 { background: linear-gradient(135deg, #d4a84b, #a0764a); color: #fff; }
.rank-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.rank-title {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
  font-weight: 500;
}
.rank-stat { font-size: 11px; color: #bbb; }
</style>
