<template>
  <div class="content-card" @click="goDetail">
    <!-- 封面图 -->
    <div class="card-cover">
      <img v-if="firstCover" :src="firstCover" :alt="content.title" class="cover-img" />
      <div v-else class="cover-placeholder">
        <el-tag size="small" :type="typeColor" class="cover-type-tag">{{ content.type }}</el-tag>
      </div>
      <el-tag v-if="firstCover" size="small" :type="typeColor" class="float-type-tag">
        {{ content.type }}
      </el-tag>
    </div>

    <!-- 卡片内容 -->
    <div class="card-body">
      <h3 class="card-title">{{ content.title }}</h3>
      <div class="card-author">
        <el-avatar :src="content.avatar" :size="18">{{ content.username?.charAt(0) }}</el-avatar>
        <span class="author-name">{{ content.username }}</span>
      </div>
      <div class="card-stats">
        <span><el-icon><View /></el-icon> {{ content.viewCount }}</span>
        <span><el-icon><Star /></el-icon> {{ content.likeCount }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { View, Star } from '@element-plus/icons-vue'

const props = defineProps<{ content: any }>()
const router = useRouter()

const typeColorMap: Record<string, any> = {
  '小说': 'primary', '散文': 'success', '诗词': 'warning',
  '随笔': 'info', '名人名言': 'danger', '杂谈': ''
}
const typeColor = computed(() => typeColorMap[props.content.type] || '')

// 兼容多图：取第一张作为缩略图
const firstCover = computed(() => {
  const c = props.content.coverImage
  if (!c) return ''
  return c.split(',')[0]?.trim() || ''
})

const goDetail = () => router.push(`/detail/${props.content.id}`)
</script>

<style scoped>
.content-card {
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #eee;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.content-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.12);
}

/* 封面区 */
.card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 4;
  overflow: hidden;
  flex-shrink: 0;
}
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #c8dcef 100%);
}
.cover-type-tag { font-size: 12px; }
.float-type-tag {
  position: absolute;
  top: 8px;
  left: 8px;
}

/* 内容区 */
.card-body {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
}
.card-author {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #888;
}
.author-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-stats {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #aaa;
  margin-top: auto;
}
.card-stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}
</style>
