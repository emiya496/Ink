<template>
  <div class="user-profile-page">
    <NavBar />
    <div class="profile-layout">
      <!-- 用户信息头部 -->
      <div class="profile-header" v-if="profile">
        <el-avatar :src="profile.avatar" :size="80" class="profile-avatar">
          {{ profile.username?.charAt(0)?.toUpperCase() }}
        </el-avatar>
        <div class="profile-info">
          <h2 class="profile-name">{{ profile.username }}</h2>
          <p class="profile-bio">{{ profile.bio || '这个人很懒，什么都没写~' }}</p>
          <div class="profile-stats">
            <span class="stat-item"><strong>{{ profile.followersCount }}</strong> 粉丝</span>
            <span class="stat-item"><strong>{{ profile.followingCount }}</strong> 关注</span>
            <span class="stat-item"><strong>{{ profile.worksCount }}</strong> 作品</span>
          </div>
        </div>
        <div class="profile-actions" v-if="!isSelf && userStore.isLoggedIn()">
          <el-button
            v-if="!profile.following"
            type="primary"
            :loading="followLoading"
            @click="doFollow"
          >关注</el-button>
          <el-button
            v-else
            :loading="followLoading"
            @click="doUnfollow"
          >已关注</el-button>
        </div>
        <div class="profile-actions" v-if="isSelf">
          <el-button @click="router.push('/user')">编辑资料</el-button>
        </div>
      </div>
      <el-skeleton v-else :rows="3" animated style="margin-bottom:24px" />

      <!-- TA 的作品 -->
      <div class="works-section">
        <h3 class="works-title">TA 的作品</h3>
        <el-skeleton :rows="4" animated v-if="worksLoading" />
        <div v-else>
          <el-empty v-if="works.length === 0" description="暂无公开作品" />
          <div v-else class="works-grid">
            <ContentCard
              v-for="item in works"
              :key="item.id"
              :content="item"
              @click="router.push(`/detail/${item.id}`)"
            />
          </div>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="worksPage"
              :page-size="20"
              :total="worksTotal"
              layout="prev, pager, next"
              @current-change="loadWorks"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import ContentCard from '@/components/ContentCard.vue'
import { userApi } from '@/api/user'
import { contentApi } from '@/api/content'
import { followApi } from '@/api/follow'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = computed(() => Number(route.params.id))
const isSelf = computed(() => userStore.userInfo?.id === userId.value)

const profile = ref<any>(null)
const works = ref<any[]>([])
const worksPage = ref(1)
const worksTotal = ref(0)
const worksLoading = ref(false)
const followLoading = ref(false)

const loadProfile = async () => {
  const res = await userApi.getProfile(userId.value)
  profile.value = res.data
}

const loadWorks = async () => {
  worksLoading.value = true
  try {
    const res = await contentApi.getUserWorks(userId.value, worksPage.value, 20)
    works.value = res.data.list
    worksTotal.value = res.data.total
  } finally {
    worksLoading.value = false
  }
}

const doFollow = async () => {
  followLoading.value = true
  try {
    await followApi.follow(userId.value)
    profile.value.following = true
    profile.value.followersCount++
    ElMessage.success('关注成功')
  } finally {
    followLoading.value = false
  }
}

const doUnfollow = async () => {
  followLoading.value = true
  try {
    await followApi.unfollow(userId.value)
    profile.value.following = false
    profile.value.followersCount--
    ElMessage.success('已取消关注')
  } finally {
    followLoading.value = false
  }
}

onMounted(async () => {
  await loadProfile()
  loadWorks()
})

watch(userId, async () => {
  profile.value = null
  works.value = []
  worksPage.value = 1
  await loadProfile()
  loadWorks()
})
</script>

<style scoped>
.user-profile-page { min-height: 100vh; background: #f0f2f5; }
.profile-layout {
  max-width: 900px;
  margin: 24px auto;
  padding: 0 20px;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  margin-bottom: 20px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
  border: 1px solid rgba(0,0,0,0.04);
}
.profile-avatar { flex-shrink: 0; }
.profile-info { flex: 1; min-width: 0; }
.profile-name { font-size: 22px; font-weight: 700; margin: 0 0 8px; color: #1a1a1a; }
.profile-bio { font-size: 14px; color: #888; margin: 0 0 12px; }
.profile-stats { display: flex; gap: 24px; }
.stat-item { font-size: 14px; color: #666; }
.stat-item strong { font-size: 18px; font-weight: 700; color: #222; margin-right: 4px; }
.profile-actions { flex-shrink: 0; }
.works-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
  border: 1px solid rgba(0,0,0,0.04);
}
.works-title { font-size: 16px; font-weight: 600; color: #333; margin: 0 0 16px; }
.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
</style>
