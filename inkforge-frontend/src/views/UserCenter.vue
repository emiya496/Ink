<template>
  <div class="user-center-page">
    <NavBar />
    <div class="user-center-layout">
      <!-- 左侧用户信息 -->
      <div class="user-profile-card">
        <div class="avatar-section">
          <el-avatar :src="userInfo?.avatar" :size="80" class="big-avatar">
            {{ userInfo?.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <h3 class="username">{{ userInfo?.username }}</h3>
          <el-tag :type="userInfo?.role === 'admin' ? 'danger' : 'primary'" size="small">
            {{ userInfo?.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
          <p class="join-date">注册时间：{{ formatDate(userInfo?.createTime) }}</p>
          <div class="profile-stats" v-if="profileStats">
            <span class="stat-chip" @click="activeTab = 'followers'"><strong>{{ profileStats.followersCount }}</strong> 粉丝</span>
            <span class="stat-chip" @click="activeTab = 'following'"><strong>{{ profileStats.followingCount }}</strong> 关注</span>
            <span class="stat-chip"><strong>{{ profileStats.worksCount }}</strong> 作品</span>
          </div>
        </div>

        <!-- 个人简介 -->
        <div class="edit-section">
          <p class="section-label">个人简介：</p>
          <el-input
            v-model="newBio"
            type="textarea"
            :rows="3"
            :placeholder="userInfo?.bio || '介绍一下自己吧...'"
            maxlength="200"
            show-word-limit
          />
          <el-button
            type="primary"
            size="small"
            :loading="updatingBio"
            @click="doUpdateBio"
            style="margin-top:8px;width:100%"
          >
            保存简介
          </el-button>
        </div>

        <!-- 修改头像 -->
        <div class="edit-avatar">
          <p style="font-size:13px;color:#999;margin-bottom:4px">更新头像：</p>
          <p style="font-size:12px;color:#bbb;margin-bottom:8px">图片大小不超过 10 MB</p>
          <!-- 本地文件上传 -->
          <div class="upload-btn-wrap">
            <el-button size="small" style="width:100%" @click="triggerAvatarFile">
              选择本地图片
            </el-button>
            <input
              ref="avatarFileInput"
              type="file"
              accept="image/*"
              style="display:none"
              @change="onAvatarFileChange"
            />
          </div>
          <div style="text-align:center;font-size:12px;color:#ccc;margin:6px 0">— 或者填写 URL —</div>
          <el-input v-model="newAvatar" placeholder="输入图片URL" size="small" />
          <el-button
            type="primary"
            size="small"
            :loading="avatarUploading"
            @click="updateAvatar"
            style="margin-top:8px;width:100%"
          >
            保存头像
          </el-button>
        </div>

        <!-- 邮箱绑定 -->
        <div class="edit-email">
          <p style="font-size:13px;color:#999;margin-bottom:6px">绑定邮箱：</p>
          <div class="email-display">
            <span v-if="userInfo?.email" class="email-value">{{ userInfo.email }}</span>
            <span v-else class="email-empty">未绑定</span>
          </div>
          <el-button
            size="small"
            style="margin-top:8px;width:100%"
            @click="openEmailDialog"
          >
            {{ userInfo?.email ? '更换邮箱' : '绑定邮箱' }}
          </el-button>
        </div>

        <!-- 修改用户名 -->
        <div class="edit-section">
          <p class="section-label">修改用户名：</p>
          <el-input v-model="newUsername" :placeholder="userInfo?.username" size="small" />
          <el-button
            type="primary"
            size="small"
            :loading="updatingUsername"
            @click="doUpdateUsername"
            style="margin-top:8px;width:100%"
          >
            保存用户名
          </el-button>
        </div>

        <!-- 修改密码 -->
        <div class="edit-section">
          <p class="section-label">修改密码：</p>
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="当前密码" size="small" show-password style="margin-bottom:6px" />
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="新密码（6-30字符）" size="small" show-password style="margin-bottom:6px" />
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="确认新密码" size="small" show-password />
          <el-button
            type="primary"
            size="small"
            :loading="changingPassword"
            @click="doChangePassword"
            style="margin-top:8px;width:100%"
          >
            保存密码
          </el-button>
        </div>

        <!-- 注销账户 -->
        <div class="delete-account-section">
          <el-button type="danger" size="small" plain style="width:100%" @click="openDeleteDialog">
            注销账户
          </el-button>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="user-content">
        <el-tabs v-model="activeTab" @tab-change="onTabChange">

          <!-- 我的作品 -->
          <el-tab-pane label="我的作品" name="content">
            <el-skeleton :rows="4" animated v-if="contentLoading" />
            <div v-else>
              <div v-if="myContent.length === 0">
                <el-empty description="还没有发布任何作品">
                  <el-button type="primary" @click="router.push('/publish')">去创作</el-button>
                </el-empty>
              </div>
              <div v-else class="work-list">
                <div v-for="item in myContent" :key="item.id" class="work-item" style="cursor:pointer" @click="router.push(`/detail/${item.id}`)">
                  <div class="work-cover" v-if="item.coverImage">
                    <img :src="item.coverImage.split(',')[0].trim()" alt="封面" />
                  </div>
                  <div class="work-info">
                    <span class="work-title" @click="router.push(`/detail/${item.id}`)">{{ item.title }}</span>
                    <div class="work-meta">
                      <el-tag size="small" type="info">{{ item.type }}</el-tag>
                      <el-tag size="small" :type="item.status === '正常' ? 'success' : 'warning'">
                        {{ item.status }}
                      </el-tag>
                      <span class="work-time">{{ formatDate(item.createTime) }}</span>
                      <span class="work-stat">阅读 {{ item.viewCount }}</span>
                      <span class="work-stat">点赞 {{ item.likeCount }}</span>
                    </div>
                  </div>
                  <div class="work-actions">
                    <el-button size="small" plain @click.stop="router.push(`/detail/${item.id}`)">查看</el-button>
                    <el-button size="small" type="primary" plain @click.stop="router.push(`/publish/${item.id}`)">编辑</el-button>
                    <el-button size="small" type="danger" plain @click.stop="deleteContent(item.id)">删除</el-button>
                  </div>
                </div>
              </div>
              <div class="pagination-wrap">
                <el-pagination
                  v-model:current-page="contentPage"
                  :page-size="10"
                  :total="contentTotal"
                  layout="prev, pager, next"
                  @current-change="loadMyContent"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- 我的草稿 -->
          <el-tab-pane label="我的草稿" name="drafts">
            <el-skeleton :rows="4" animated v-if="draftsLoading" />
            <div v-else>
              <div v-if="myDrafts.length === 0">
                <el-empty description="暂无草稿">
                  <el-button type="primary" @click="router.push('/publish')">去创作</el-button>
                </el-empty>
              </div>
              <div v-else class="work-list">
                <div v-for="item in myDrafts" :key="item.id" class="work-item" style="cursor:pointer" @click="router.push(`/publish/${item.id}`)">
                  <div class="work-cover" v-if="item.coverImage">
                    <img :src="item.coverImage.split(',')[0].trim()" alt="封面" />
                  </div>
                  <div class="work-info">
                    <span class="work-title">{{ item.title || '（无标题）' }}</span>
                    <div class="work-meta">
                      <el-tag size="small" type="info">{{ item.type }}</el-tag>
                      <el-tag size="small" type="warning">草稿</el-tag>
                      <span class="work-time">{{ formatDate(item.createTime) }}</span>
                    </div>
                  </div>
                  <div class="work-actions">
                    <el-button size="small" type="primary" plain @click.stop="router.push(`/publish/${item.id}`)">
                      继续编辑
                    </el-button>
                    <el-button size="small" type="danger" plain @click.stop="deleteDraft(item.id)">
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
              <div class="pagination-wrap">
                <el-pagination
                  v-model:current-page="draftsPage"
                  :page-size="10"
                  :total="draftsTotal"
                  layout="prev, pager, next"
                  @current-change="loadMyDrafts"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- 我的点赞 -->
          <el-tab-pane label="我的点赞" name="likes">
            <el-skeleton :rows="4" animated v-if="likesLoading" />
            <div v-else>
              <div v-if="myLikes.length === 0">
                <el-empty description="还没有点赞任何作品" />
              </div>
              <div v-else class="work-list">
                <div v-for="item in myLikes" :key="item.id" class="work-item" style="cursor:pointer" @click="router.push(`/detail/${item.id}`)">
                  <div class="work-cover" v-if="item.coverImage">
                    <img :src="item.coverImage.split(',')[0].trim()" alt="封面" />
                  </div>
                  <div class="work-info">
                    <span class="work-title">{{ item.title }}</span>
                    <div class="work-meta">
                      <el-tag size="small" type="info">{{ item.type }}</el-tag>
                      <span class="work-time">{{ formatDate(item.createTime) }}</span>
                      <span class="work-stat">阅读 {{ item.viewCount }}</span>
                      <span class="work-stat">点赞 {{ item.likeCount }}</span>
                    </div>
                  </div>
                  <div class="work-actions">
                    <el-button size="small" plain @click.stop="router.push(`/detail/${item.id}`)">查看</el-button>
                    <el-button size="small" type="warning" plain @click.stop="cancelLike(item.id)">取消点赞</el-button>
                  </div>
                </div>
              </div>
              <div class="pagination-wrap">
                <el-pagination
                  v-model:current-page="likesPage"
                  :page-size="20"
                  :total="likesTotal"
                  layout="prev, pager, next"
                  @current-change="loadMyLikes"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- 我的关注 -->
          <el-tab-pane label="我的关注" name="following">
            <el-skeleton :rows="4" animated v-if="followingLoading" />
            <div v-else>
              <div v-if="myFollowing.length === 0">
                <el-empty description="还没有关注任何用户" />
              </div>
              <div v-else class="user-list">
                <div v-for="user in myFollowing" :key="user.id" class="user-item">
                  <el-avatar :src="user.avatar" :size="48" style="cursor:pointer;flex-shrink:0" @click="router.push(`/user/${user.id}`)">
                    {{ user.username?.charAt(0)?.toUpperCase() }}
                  </el-avatar>
                  <div class="user-item-info" @click="router.push(`/user/${user.id}`)">
                    <span class="user-item-name">{{ user.username }}</span>
                    <span class="user-item-bio">{{ user.bio || '这个人很懒，什么都没写~' }}</span>
                    <span class="user-item-stat">粉丝 {{ user.followersCount }}</span>
                  </div>
                  <el-button size="small" plain @click="doUnfollow(user.id)">取消关注</el-button>
                </div>
              </div>
              <div class="pagination-wrap">
                <el-pagination
                  v-model:current-page="followingPage"
                  :page-size="20"
                  :total="followingTotal"
                  layout="prev, pager, next"
                  @current-change="loadMyFollowing"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- 我的粉丝 -->
          <el-tab-pane label="我的粉丝" name="followers">
            <el-skeleton :rows="4" animated v-if="followersLoading" />
            <div v-else>
              <div v-if="myFollowers.length === 0">
                <el-empty description="还没有粉丝" />
              </div>
              <div v-else class="user-list">
                <div v-for="user in myFollowers" :key="user.id" class="user-item">
                  <el-avatar :src="user.avatar" :size="48" style="cursor:pointer;flex-shrink:0" @click="router.push(`/user/${user.id}`)">
                    {{ user.username?.charAt(0)?.toUpperCase() }}
                  </el-avatar>
                  <div class="user-item-info" @click="router.push(`/user/${user.id}`)">
                    <span class="user-item-name">{{ user.username }}</span>
                    <span class="user-item-bio">{{ user.bio || '这个人很懒，什么都没写~' }}</span>
                    <span class="user-item-stat">粉丝 {{ user.followersCount }}</span>
                  </div>
                  <el-button size="small" plain type="danger" @click="doRemoveFollower(user.id)">移除粉丝</el-button>
                </div>
              </div>
              <div class="pagination-wrap">
                <el-pagination
                  v-model:current-page="followersPage"
                  :page-size="20"
                  :total="followersTotal"
                  layout="prev, pager, next"
                  @current-change="loadMyFollowers"
                />
              </div>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>
  </div>

  <!-- 邮箱绑定/更换 Dialog -->
  <el-dialog
    v-model="emailDialogVisible"
    :title="userInfo?.email ? '更换邮箱' : '绑定邮箱'"
    width="400px"
    :close-on-click-modal="false"
  >
    <el-form :model="emailForm" label-width="80px">
      <el-form-item label="新邮箱">
        <el-input v-model="emailForm.email" placeholder="请输入邮箱地址" />
      </el-form-item>
      <el-form-item label="验证码">
        <div style="display:flex;gap:8px;width:100%">
          <el-input v-model="emailForm.code" placeholder="6位验证码" style="flex:1" />
          <el-button
            :disabled="emailCooldown > 0"
            :loading="sendingCode"
            @click="sendCode"
            style="white-space:nowrap"
          >
            {{ emailCooldown > 0 ? `${emailCooldown}s后重发` : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emailDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="bindingEmail" @click="confirmEmail">确认</el-button>
    </template>
  </el-dialog>

  <!-- 注销账户 Dialog -->
  <el-dialog v-model="deleteDialogVisible" title="注销账户" width="420px" :close-on-click-modal="false">
    <el-alert type="error" show-icon :closable="false" style="margin-bottom:16px">
      <template #title>账户注销后所有数据将永久删除，此操作不可撤销！</template>
    </el-alert>
    <el-form label-width="80px">
      <el-form-item label="当前密码">
        <el-input v-model="deleteForm.password" type="password" show-password placeholder="请输入当前密码" />
      </el-form-item>
      <el-form-item v-if="userInfo?.email" label="邮箱验证码">
        <div style="display:flex;gap:8px;width:100%">
          <el-input v-model="deleteForm.code" placeholder="6位验证码" style="flex:1" />
          <el-button
            :disabled="deleteCooldown > 0"
            :loading="sendingDeleteCode"
            @click="sendDeleteCode"
            style="white-space:nowrap"
          >
            {{ deleteCooldown > 0 ? `${deleteCooldown}s后重发` : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="deleteDialogVisible = false">取消</el-button>
      <el-button type="danger" :loading="deletingAccount" @click="confirmDelete">确认注销</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { userApi } from '@/api/user'
import { contentApi } from '@/api/content'
import { followApi } from '@/api/follow'
import { uploadApi } from '@/api/upload'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const userInfo = ref<any>(null)
const newAvatar = ref('')
const avatarUploading = ref(false)
const avatarFileInput = ref<HTMLInputElement | null>(null)

const triggerAvatarFile = () => avatarFileInput.value?.click()

const onAvatarFileChange = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10 MB，请压缩后重试')
    if (avatarFileInput.value) avatarFileInput.value.value = ''
    return
  }
  avatarUploading.value = true
  try {
    const res = await uploadApi.image(file)
    newAvatar.value = res.data
    ElMessage.success('图片上传成功，点击"保存头像"完成设置')
  } finally {
    avatarUploading.value = false
    // 清空 input 以便重复选同一文件
    if (avatarFileInput.value) avatarFileInput.value.value = ''
  }
}
const activeTab = ref((route.query.tab as string) || 'content')
const myContent = ref<any[]>([])
const contentPage = ref(1)
const contentTotal = ref(0)
const contentLoading = ref(false)

// 草稿
const myDrafts = ref<any[]>([])
const draftsPage = ref(1)
const draftsTotal = ref(0)
const draftsLoading = ref(false)

// 我的点赞
const myLikes = ref<any[]>([])
const likesPage = ref(1)
const likesTotal = ref(0)
const likesLoading = ref(false)

// 我的关注
const myFollowing = ref<any[]>([])
const followingPage = ref(1)
const followingTotal = ref(0)
const followingLoading = ref(false)

// 我的粉丝
const myFollowers = ref<any[]>([])
const followersPage = ref(1)
const followersTotal = ref(0)
const followersLoading = ref(false)

// 统计数据（粉丝数、关注数、作品数）
const profileStats = ref<any>(null)

const formatDate = (d: string) => d ? new Date(d).toLocaleDateString('zh-CN') : ''

const loadUserInfo = async () => {
  const res = await userApi.getInfo()
  userInfo.value = res.data
  // 加载统计数据
  const profileRes = await userApi.getProfile(userInfo.value.id)
  profileStats.value = profileRes.data
}

const updateAvatar = async () => {
  if (!newAvatar.value) return
  await userApi.updateInfo({ avatar: newAvatar.value })
  userStore.setUserInfo({ ...userStore.userInfo!, avatar: newAvatar.value })
  userInfo.value.avatar = newAvatar.value
  ElMessage.success('头像已更新')
  newAvatar.value = ''
}

// 个人简介
const newBio = ref('')
const updatingBio = ref(false)

const doUpdateBio = async () => {
  updatingBio.value = true
  try {
    await userApi.updateBio(newBio.value.trim())
    userInfo.value.bio = newBio.value.trim()
    ElMessage.success('简介已更新')
    newBio.value = ''
  } finally {
    updatingBio.value = false
  }
}

const loadMyContent = async () => {
  contentLoading.value = true
  try {
    const res = await contentApi.my({ page: contentPage.value, size: 10 })
    myContent.value = res.data.list
    contentTotal.value = res.data.total
  } finally {
    contentLoading.value = false
  }
}

const loadMyDrafts = async () => {
  draftsLoading.value = true
  try {
    const res = await contentApi.myDrafts({ page: draftsPage.value, size: 10 })
    myDrafts.value = res.data.list
    draftsTotal.value = res.data.total
  } finally {
    draftsLoading.value = false
  }
}

const deleteContent = async (id: number) => {
  await ElMessageBox.confirm(
    '确认删除该作品？删除后无法恢复。\n提示：若只是不想公开展示，可将作品改为「私密」状态。',
    '警告',
    { type: 'warning', confirmButtonText: '仍要删除', cancelButtonText: '取消' }
  )
  await contentApi.delete(id)
  ElMessage.success('作品已删除')
  loadMyContent()
}

const deleteDraft = async (id: number) => {
  await ElMessageBox.confirm('确认删除该草稿？', '提示', { type: 'warning' })
  await contentApi.delete(id)
  ElMessage.success('草稿已删除')
  loadMyDrafts()
}

const onTabChange = (name: string) => {
  if (name === 'drafts' && myDrafts.value.length === 0) {
    loadMyDrafts()
  }
  if (name === 'likes' && myLikes.value.length === 0) {
    loadMyLikes()
  }
  if (name === 'following' && myFollowing.value.length === 0) {
    loadMyFollowing()
  }
  if (name === 'followers' && myFollowers.value.length === 0) {
    loadMyFollowers()
  }
}

const loadMyLikes = async () => {
  likesLoading.value = true
  try {
    const res = await contentApi.myLikes(likesPage.value, 20)
    myLikes.value = res.data.list
    likesTotal.value = res.data.total
  } finally {
    likesLoading.value = false
  }
}

const loadMyFollowing = async () => {
  followingLoading.value = true
  try {
    const res = await followApi.getFollowing(userInfo.value?.id, followingPage.value, 20)
    myFollowing.value = res.data.list
    followingTotal.value = res.data.total
  } finally {
    followingLoading.value = false
  }
}

const loadMyFollowers = async () => {
  followersLoading.value = true
  try {
    const res = await followApi.getFollowers(userInfo.value?.id, followersPage.value, 20)
    myFollowers.value = res.data.list
    followersTotal.value = res.data.total
  } finally {
    followersLoading.value = false
  }
}

const doUnfollow = async (userId: number) => {
  await followApi.unfollow(userId)
  ElMessage.success('已取消关注')
  myFollowing.value = myFollowing.value.filter(u => u.id !== userId)
  followingTotal.value--
}

const cancelLike = async (contentId: number) => {
  await contentApi.like(contentId)
  ElMessage.success('已取消点赞')
  myLikes.value = myLikes.value.filter(item => item.id !== contentId)
  likesTotal.value--
}

const doRemoveFollower = async (userId: number) => {
  await followApi.removeFollower(userId)
  ElMessage.success('已移除粉丝')
  myFollowers.value = myFollowers.value.filter(u => u.id !== userId)
  followersTotal.value--
}

// tab 切换时：同步 URL query（供浏览器历史记录保存），并触发懒加载
watch(activeTab, (name) => {
  router.replace({ query: { ...route.query, tab: name } })
  onTabChange(name)
})

// 邮箱绑定/更换
const emailDialogVisible = ref(false)
const emailForm = ref({ email: '', code: '' })
const emailCooldown = ref(0)
const sendingCode = ref(false)
const bindingEmail = ref(false)
let cooldownTimer: ReturnType<typeof setInterval> | null = null

const openEmailDialog = () => {
  emailForm.value = { email: '', code: '' }
  emailCooldown.value = 0
  emailDialogVisible.value = true
}

const sendCode = async () => {
  if (!emailForm.value.email) {
    ElMessage.warning('请先填写邮箱地址')
    return
  }
  sendingCode.value = true
  try {
    const purpose = userInfo.value?.email ? 'change' : 'bind'
    await userApi.sendEmailCode({ email: emailForm.value.email, purpose })
    ElMessage.success('验证码已发送，请查收邮件')
    emailCooldown.value = 60
    cooldownTimer = setInterval(() => {
      emailCooldown.value--
      if (emailCooldown.value <= 0 && cooldownTimer) {
        clearInterval(cooldownTimer)
        cooldownTimer = null
      }
    }, 1000)
  } finally {
    sendingCode.value = false
  }
}

const confirmEmail = async () => {
  if (!emailForm.value.email || !emailForm.value.code) {
    ElMessage.warning('请填写完整邮箱和验证码')
    return
  }
  bindingEmail.value = true
  try {
    const isChange = !!userInfo.value?.email
    if (isChange) {
      await userApi.changeEmail({ email: emailForm.value.email, code: emailForm.value.code })
    } else {
      await userApi.bindEmail({ email: emailForm.value.email, code: emailForm.value.code })
    }
    ElMessage.success(isChange ? '邮箱已更换' : '邮箱绑定成功')
    emailDialogVisible.value = false
    // 刷新用户信息
    await loadUserInfo()
  } finally {
    bindingEmail.value = false
  }
}

// 修改用户名
const newUsername = ref('')
const updatingUsername = ref(false)

const doUpdateUsername = async () => {
  if (!newUsername.value.trim()) {
    ElMessage.warning('请输入新用户名')
    return
  }
  if (newUsername.value.trim().length < 3 || newUsername.value.trim().length > 20) {
    ElMessage.warning('用户名长度需为3-20字符')
    return
  }
  updatingUsername.value = true
  try {
    await userApi.updateUsername({ newUsername: newUsername.value.trim() })
    ElMessage.success('用户名已更新，下次登录生效')
    userInfo.value.username = newUsername.value.trim()
    userStore.setUserInfo({ ...userStore.userInfo!, username: newUsername.value.trim() })
    newUsername.value = ''
  } finally {
    updatingUsername.value = false
  }
}

// 修改密码
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const changingPassword = ref(false)

const doChangePassword = async () => {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword || !pwdForm.value.confirmPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (pwdForm.value.newPassword.length < 6 || pwdForm.value.newPassword.length > 30) {
    ElMessage.warning('新密码长度需为6-30字符')
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  changingPassword.value = true
  try {
    await userApi.changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword,
    })
    ElMessage.success('密码已修改，请重新登录')
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    userStore.logout()
    router.push('/login')
  } finally {
    changingPassword.value = false
  }
}

// 注销账户
const deleteDialogVisible = ref(false)
const deleteForm = ref({ password: '', code: '' })
const deleteCooldown = ref(0)
const sendingDeleteCode = ref(false)
const deletingAccount = ref(false)
let deleteCodeTimer: ReturnType<typeof setInterval> | null = null

const openDeleteDialog = () => {
  deleteForm.value = { password: '', code: '' }
  deleteCooldown.value = 0
  deleteDialogVisible.value = true
}

const sendDeleteCode = async () => {
  sendingDeleteCode.value = true
  try {
    await userApi.sendDeleteCode()
    ElMessage.success('验证码已发送，请查收邮件')
    deleteCooldown.value = 60
    deleteCodeTimer = setInterval(() => {
      deleteCooldown.value--
      if (deleteCooldown.value <= 0 && deleteCodeTimer) {
        clearInterval(deleteCodeTimer)
        deleteCodeTimer = null
      }
    }, 1000)
  } finally {
    sendingDeleteCode.value = false
  }
}

const confirmDelete = async () => {
  if (!deleteForm.value.password) {
    ElMessage.warning('请填写当前密码')
    return
  }
  if (userInfo.value?.email && !deleteForm.value.code) {
    ElMessage.warning('请填写邮箱验证码')
    return
  }
  try {
    await ElMessageBox.confirm(
      '账户注销后所有数据将永久删除，无法恢复。确定继续吗？',
      '最终确认',
      { confirmButtonText: '确认注销', cancelButtonText: '取消', type: 'error' }
    )
  } catch { return }
  deletingAccount.value = true
  try {
    await userApi.deleteAccount({ password: deleteForm.value.password, code: deleteForm.value.code })
    ElMessage.success('账户已注销')
    userStore.logout()
    router.push('/')
  } finally {
    deletingAccount.value = false
  }
}

onMounted(async () => {
  await loadUserInfo()
  loadMyContent()
  // 若初始 tab 不是默认的 'content'（说明从历史记录恢复过来），立即加载对应数据
  if (activeTab.value !== 'content') {
    onTabChange(activeTab.value)
  }
})
</script>

<style scoped>
.user-center-page { min-height: calc(100vh + 1px); background: #f0f2f5; }
.user-center-layout {
  max-width: 1100px;
  margin: 24px auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 20px;
}
.user-profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  height: fit-content;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.avatar-section { text-align: center; margin-bottom: 20px; }
.big-avatar { margin-bottom: 12px; }
.username { font-size: 18px; font-weight: 700; margin: 8px 0; color: #1a1a1a; }
.join-date { font-size: 12px; color: #bbb; margin-top: 6px; }
.profile-stats { display: flex; justify-content: center; gap: 16px; margin-top: 12px; }
.stat-chip {
  font-size: 13px; color: #666; cursor: pointer;
  padding: 4px 8px; border-radius: 6px; transition: background 0.2s;
}
.stat-chip:hover { background: #f0f2f5; color: #409eff; }
.stat-chip strong { font-size: 16px; font-weight: 700; color: #222; margin-right: 3px; }
.edit-avatar { padding-top: 16px; border-top: 1px solid #f5f5f5; }
.upload-btn-wrap { margin-bottom: 4px; }
.edit-email { padding-top: 16px; border-top: 1px solid #f5f5f5; margin-top: 16px; }
.email-display { font-size: 13px; min-height: 20px; }
.email-value { color: #333; word-break: break-all; }
.email-empty { color: #bbb; font-style: italic; }
.edit-section { padding-top: 16px; border-top: 1px solid #f5f5f5; margin-top: 16px; }
.section-label { font-size: 12px; color: #aaa; margin: 0 0 6px; font-weight: 500; letter-spacing: 0.3px; }
.delete-account-section { padding-top: 16px; border-top: 1px solid #f5f5f5; margin-top: 16px; }
.user-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; padding-bottom: 8px; }

/* 作品/草稿通用列表 */
.work-list { display: flex; flex-direction: column; gap: 10px; }
.work-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 10px;
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.2s;
  background: #fafafa;
}
.work-item:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  border-color: rgba(64, 158, 255, 0.2);
  transform: translateX(2px);
  background: #fff;
}

/* 封面缩略图 */
.work-cover {
  width: 44px;
  height: 58px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}
.work-cover img { width: 100%; height: 100%; object-fit: cover; }

.work-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 5px; }
.work-title {
  font-size: 15px;
  font-weight: 600;
  color: #222;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s;
}
.work-title:hover { color: #409eff; }
.work-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.work-time { font-size: 12px; color: #bbb; }
.work-stat { font-size: 12px; color: #ccc; }
.work-actions { display: flex; gap: 8px; flex-shrink: 0; }

/* 关注用户列表 */
.user-list { display: flex; flex-direction: column; gap: 10px; }
.user-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 10px;
  background: #fafafa;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.user-item:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  border-color: rgba(64,158,255,0.2);
  background: #fff;
}
.user-item-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 3px; cursor: pointer; }
.user-item-name { font-size: 15px; font-weight: 600; color: #222; }
.user-item-bio { font-size: 13px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.user-item-stat { font-size: 12px; color: #bbb; }
</style>
