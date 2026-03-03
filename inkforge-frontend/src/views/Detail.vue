<template>
  <div class="detail-page">
    <NavBar />
    <div class="detail-layout" v-if="content">
      <!-- 主内容区 -->
      <div class="main-area">
        <!-- 文章头部 -->
        <div class="article-header">
          <!-- 封面图（支持多图，第一张大图展示，其余缩略图，点击查看大图） -->
          <div v-if="coverImages.length > 0" class="article-covers">
            <div class="article-cover-main" @click="openCoverPreview(0)">
              <img :src="coverImages[0]" alt="封面" />
            </div>
            <div v-if="coverImages.length > 1" class="article-cover-thumbs">
              <div
                v-for="(img, idx) in coverImages.slice(1)"
                :key="idx"
                class="cover-thumb-item"
                @click="openCoverPreview(Number(idx) + 1)"
              >
                <img :src="img" :alt="`配图${Number(idx)+2}`" />
              </div>
            </div>
          </div>
          <div class="article-type-tags">
            <el-tag :type="typeColor">{{ content.type }}</el-tag>
            <el-tag v-for="tag in content.tags" :key="tag.id" effect="plain" size="small">#{{ tag.tagName }}</el-tag>
          </div>
          <h1 class="article-title">{{ content.title }}</h1>
          <div class="article-meta">
            <el-avatar :src="content.avatar" :size="32">{{ content.username?.charAt(0) }}</el-avatar>
            <span class="author-name">{{ content.username }}</span>
            <span class="dot">·</span>
            <span>{{ formatDate(content.createTime) }}</span>
            <span class="dot">·</span>
            <span><el-icon><View /></el-icon> {{ content.viewCount }}</span>
          </div>
          <div class="article-actions">
            <el-button
              :icon="content.liked ? 'StarFilled' : 'Star'"
              :type="content.liked ? 'warning' : 'default'"
              round
              @click="doLike"
            >点赞 {{ content.likeCount }}</el-button>
            <el-button
              :icon="favorited ? 'CollectionTag' : 'Collection'"
              :type="favorited ? 'success' : 'default'"
              round
              @click="toggleFavorite"
            >{{ favorited ? '已收藏' : '收藏' }}</el-button>
            <el-button
              v-if="isOwner"
              icon="Edit"
              round
              @click="router.push(`/publish/${content.id}`)"
            >编辑</el-button>
            <el-button
              v-if="isOwner || isAdmin"
              icon="Delete"
              type="danger"
              round
              @click="doDelete"
            >删除</el-button>
          </div>
        </div>

        <!-- 小说章节列表 -->
        <div v-if="content.type === '小说' && chapters.length > 0" class="chapter-nav">
          <h3>章节目录</h3>
          <div class="chapter-list">
            <span
              v-for="(ch, idx) in chapters"
              :key="ch.id"
              :class="['chapter-item', { active: activeChapter === idx }]"
              @click="activeChapter = idx"
            >第{{ idx+1 }}章：{{ ch.chapterTitle || '无标题' }}</span>
          </div>
        </div>

        <!-- 正文 -->
        <div class="article-body">
          <div v-if="content.type === '小说' && chapters.length > 0">
            <h3 class="chapter-title-show">{{ chapters[activeChapter]?.chapterTitle || `第${activeChapter+1}章` }}</h3>
            <div class="article-content" v-html="chapters[activeChapter]?.chapterContent" />
            <div class="chapter-nav-btns">
              <el-button :disabled="activeChapter === 0" @click="activeChapter--">上一章</el-button>
              <el-button :disabled="activeChapter === chapters.length-1" @click="activeChapter++">下一章</el-button>
            </div>
          </div>
          <div v-else class="article-content" v-html="content.content" />
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <h3 class="section-title">评论 ({{ commentTotal }})</h3>
          <!-- 发表评论 -->
          <div v-if="userStore.isLoggedIn()" class="comment-input-area">
            <el-avatar :src="userStore.userInfo?.avatar" :size="36">{{ userStore.userInfo?.username?.charAt(0) }}</el-avatar>
            <div class="comment-input-wrap">
              <el-input
                v-model="commentText"
                type="textarea"
                :rows="3"
                placeholder="发表你的评论..."
                maxlength="500"
                show-word-limit
              />
              <el-button type="primary" @click="submitComment" :loading="commenting" class="comment-submit-btn">发表评论</el-button>
            </div>
          </div>
          <el-alert v-else type="info" :closable="false" show-icon>
            <span>请 <el-link @click="router.push('/login')">登录</el-link> 后发表评论</span>
          </el-alert>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :src="comment.avatar" :size="36">{{ comment.username?.charAt(0) }}</el-avatar>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.username }}</span>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                  <el-button
                    v-if="comment.userId === userStore.userInfo?.id || isAdmin"
                    text
                    type="danger"
                    size="small"
                    @click="deleteComment(comment.id)"
                  >删除</el-button>
                </div>
                <p class="comment-text">{{ comment.content }}</p>
              </div>
            </div>
            <el-empty v-if="comments.length === 0" description="暂无评论，来发表第一条评论吧" />
            <div class="comment-pagination" v-if="commentTotal > 20">
              <el-pagination
                v-model:current-page="commentPage"
                :page-size="20"
                :total="commentTotal"
                layout="prev, pager, next"
                @current-change="loadComments"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧 AI 阅读助手 -->
      <div class="ai-reader-panel">
        <div class="ai-panel-header">
          <el-icon><MagicStick /></el-icon>
          <span>AI 阅读助手</span>
        </div>

        <div class="ai-functions">
          <el-button class="ai-func-btn" @click="callAi('summary')" :loading="aiLoading === 'summary'" plain>
            📋 本文摘要
          </el-button>
          <el-button class="ai-func-btn" @click="callAi('keywords')" :loading="aiLoading === 'keywords'" plain>
            🔑 关键词提取
          </el-button>
          <el-button class="ai-func-btn" @click="callAi('sentiment')" :loading="aiLoading === 'sentiment'" plain>
            💫 情感分析
          </el-button>
          <el-button class="ai-func-btn" @click="callAi('style')" :loading="aiLoading === 'style'" plain>
            🎨 文风分析
          </el-button>
        </div>

        <!-- AI 问答 -->
        <div class="ai-qa-section">
          <p class="ai-qa-label">🤖 AI 问答</p>
          <el-input
            v-model="qaQuestion"
            placeholder="对这篇文章有什么问题？"
            @keyup.enter="callAi('qa')"
          />
          <el-button type="primary" @click="callAi('qa')" :loading="aiLoading === 'qa'" style="width:100%;margin-top:8px">
            提问
          </el-button>
        </div>

        <!-- AI 结果展示 -->
        <div v-if="aiResult" class="ai-result-box">
          <div class="ai-result-label">{{ aiResultLabel }}</div>
          <div class="ai-result-text">{{ aiResult }}</div>
        </div>
      </div>
    </div>

    <el-skeleton v-else :rows="10" animated style="max-width:1200px;margin:20px auto;padding:0 20px" />
  </div>

  <!-- 封面大图预览 -->
  <el-dialog v-model="coverPreviewVisible" width="auto" :show-close="true" align-center>
    <div style="display:flex;align-items:center;gap:12px">
      <el-button :disabled="coverPreviewIndex === 0" icon="ArrowLeft" circle @click="coverPreviewIndex--" />
      <img :src="coverImages[coverPreviewIndex]" style="max-width:75vw;max-height:80vh;display:block;border-radius:6px" />
      <el-button :disabled="coverPreviewIndex >= coverImages.length - 1" icon="ArrowRight" circle @click="coverPreviewIndex++" />
    </div>
    <div style="text-align:center;color:#999;font-size:13px;margin-top:8px">
      {{ coverPreviewIndex + 1 }} / {{ coverImages.length }}
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { contentApi, chapterApi } from '@/api/content'
import { commentApi, favoriteApi } from '@/api/tag'
import { aiApi } from '@/api/ai'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const contentId = Number(route.params.id)

const content = ref<any>(null)
const chapters = ref<any[]>([])
const activeChapter = ref(0)
const favorited = ref(false)

// 封面多图支持
const coverImages = computed(() => {
  if (!content.value?.coverImage) return []
  return content.value.coverImage.split(',').map((s: string) => s.trim()).filter(Boolean)
})
const coverPreviewVisible = ref(false)
const coverPreviewIndex = ref(0)
const openCoverPreview = (idx: number) => {
  coverPreviewIndex.value = idx
  coverPreviewVisible.value = true
}

const comments = ref<any[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentText = ref('')
const commenting = ref(false)

const aiLoading = ref<string | null>(null)
const aiResult = ref('')
const aiResultLabel = ref('')
const qaQuestion = ref('')

const typeColorMap: Record<string, any> = {
  '小说': 'primary', '散文': 'success', '诗词': 'warning', '随笔': 'info', '名人名言': 'danger'
}
const typeColor = computed(() => typeColorMap[content.value?.type] || '')
const isOwner = computed(() => content.value?.userId === userStore.userInfo?.id)
const isAdmin = computed(() => userStore.isAdmin())

const formatDate = (d: string) => d ? new Date(d).toLocaleDateString('zh-CN') : ''

const loadContent = async () => {
  const res = await contentApi.detail(contentId)
  content.value = res.data
  if (content.value.type === '小说') {
    const cRes = await chapterApi.list(contentId)
    chapters.value = cRes.data
  }
}

const checkFavorite = async () => {
  if (!userStore.isLoggedIn()) return
  const res = await favoriteApi.check(contentId)
  favorited.value = res.data
}

const loadComments = async () => {
  const res = await commentApi.list(contentId, commentPage.value, 20)
  comments.value = res.data.list
  commentTotal.value = res.data.total
}

const submitComment = async () => {
  if (!commentText.value.trim()) { ElMessage.warning('请输入评论内容'); return }
  commenting.value = true
  try {
    await commentApi.add(contentId, commentText.value.trim())
    ElMessage.success('评论成功')
    commentText.value = ''
    loadComments()
  } finally {
    commenting.value = false
  }
}

const deleteComment = async (id: number) => {
  await ElMessageBox.confirm('确认删除此评论？', '提示', { type: 'warning' })
  await commentApi.delete(id)
  ElMessage.success('已删除')
  loadComments()
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn()) { router.push('/login'); return }
  if (favorited.value) {
    await favoriteApi.remove(contentId)
    favorited.value = false
    ElMessage.success('已取消收藏')
  } else {
    await favoriteApi.add(contentId)
    favorited.value = true
    ElMessage.success('收藏成功')
  }
}

const doLike = async () => {
  if (!userStore.isLoggedIn()) { router.push('/login'); return }
  const res = await contentApi.like(contentId)
  const nowLiked: boolean = res.data
  content.value.liked = nowLiked
  content.value.likeCount += nowLiked ? 1 : -1
  ElMessage.success(nowLiked ? '点赞成功' : '已取消点赞')
}

const doDelete = async () => {
  await ElMessageBox.confirm('确认删除此内容？删除后不可恢复！', '警告', { type: 'warning' })
  await contentApi.delete(contentId)
  ElMessage.success('已删除')
  router.push('/')
}

const getArticleText = () => {
  if (content.value.type === '小说' && chapters.value.length > 0) {
    return chapters.value.map(ch => ch.chapterContent || '').join('\n').replace(/<[^>]+>/g, '')
  }
  return (content.value.content || '').replace(/<[^>]+>/g, '')
}

const aiLabelMap: Record<string, string> = {
  summary: '📋 本文摘要', keywords: '🔑 关键词', sentiment: '💫 情感分析',
  style: '🎨 文风分析', qa: '🤖 AI回答'
}

const callAi = async (type: string) => {
  const text = getArticleText()
  if (!text) { ElMessage.warning('文章内容为空'); return }
  if (!userStore.isLoggedIn()) { router.push('/login'); return }
  aiLoading.value = type
  aiResult.value = ''
  try {
    let res: any
    if (type === 'qa') {
      if (!qaQuestion.value.trim()) { ElMessage.warning('请输入问题'); return }
      res = await aiApi.qa(text, qaQuestion.value)
    } else {
      res = await (aiApi as any)[type](text)
    }
    aiResult.value = res.data
    aiResultLabel.value = aiLabelMap[type]
  } finally {
    aiLoading.value = null
  }
}

onMounted(() => {
  loadContent()
  loadComments()
  checkFavorite()
})
</script>

<style scoped>
.detail-page { min-height: 100vh; background: #f5f7fa; }
.detail-layout {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}
.main-area { display: flex; flex-direction: column; gap: 20px; }

.article-header {
  background: #fff;
  border-radius: 8px;
  padding: 28px;
  overflow: hidden;
}
.article-cover {
  margin: -28px -28px 24px -28px;
  height: 300px;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
}
.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 多图封面 */
.article-covers {
  margin: -28px -28px 24px -28px;
}
.article-cover-main {
  height: 300px;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
  cursor: pointer;
}
.article-cover-main img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: opacity 0.2s;
}
.article-cover-main:hover img { opacity: 0.9; }
.article-cover-thumbs {
  display: flex;
  gap: 6px;
  padding: 6px;
  background: #f0f0f0;
  flex-wrap: wrap;
}
.cover-thumb-item {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}
.cover-thumb-item:hover { border-color: #409eff; }
.cover-thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.article-type-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; }
.article-title { font-size: 28px; font-weight: 700; color: #1a1a1a; margin-bottom: 16px; line-height: 1.4; }
.article-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #888;
  margin-bottom: 20px;
}
.author-name { font-weight: 500; color: #555; }
.dot { color: #ccc; }
.article-actions { display: flex; gap: 10px; flex-wrap: wrap; }

.chapter-nav {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
.chapter-nav h3 { font-size: 16px; margin-bottom: 12px; color: #333; }
.chapter-list { display: flex; flex-direction: column; gap: 6px; }
.chapter-item {
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #555;
  transition: all 0.2s;
}
.chapter-item:hover { background: #f0f7ff; color: #409eff; }
.chapter-item.active { background: #ecf5ff; color: #409eff; font-weight: 600; }

.article-body {
  background: #fff;
  border-radius: 8px;
  padding: 28px;
}
.chapter-title-show { font-size: 20px; font-weight: 600; margin-bottom: 20px; color: #333; }
.article-content { font-size: 17px; line-height: 2; color: #333; }
.article-content :deep(p) { margin-bottom: 1em; }
.article-content :deep(img) { max-width: 100%; }
.chapter-nav-btns { display: flex; justify-content: space-between; margin-top: 32px; }

.comment-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}
.section-title { font-size: 18px; font-weight: 600; color: #333; margin-bottom: 20px; }
.comment-input-area { display: flex; gap: 12px; margin-bottom: 24px; }
.comment-input-wrap { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.comment-submit-btn { align-self: flex-end; }

.comment-list { display: flex; flex-direction: column; gap: 20px; }
.comment-item { display: flex; gap: 12px; }
.comment-content { flex: 1; }
.comment-header { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.comment-author { font-weight: 600; color: #333; font-size: 14px; }
.comment-time { font-size: 12px; color: #aaa; }
.comment-text { font-size: 15px; color: #444; line-height: 1.6; }
.comment-pagination { display: flex; justify-content: center; margin-top: 16px; }

/* AI 阅读助手面板 */
.ai-reader-panel {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  height: fit-content;
  position: sticky;
  top: 80px;
}
.ai-panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}
.ai-functions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 16px;
}
.ai-func-btn { width: 100%; }
.ai-qa-section { margin-bottom: 16px; }
.ai-qa-label { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 8px; }
.ai-result-box {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 14px;
}
.ai-result-label { font-size: 13px; font-weight: 600; color: #409eff; margin-bottom: 8px; }
.ai-result-text { font-size: 14px; line-height: 1.7; color: #555; white-space: pre-wrap; max-height: 400px; overflow-y: auto; }
</style>
