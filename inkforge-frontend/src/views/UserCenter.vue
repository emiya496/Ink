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
        </div>

        <!-- 修改头像 -->
        <div class="edit-avatar">
          <p style="font-size:13px;color:#999;margin-bottom:8px">更新头像：</p>
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
                <div v-for="item in myContent" :key="item.id" class="work-item">
                  <div class="work-cover" v-if="item.coverImage">
                    <img :src="item.coverImage" alt="封面" />
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
                    <el-button size="small" plain @click="router.push(`/detail/${item.id}`)">查看</el-button>
                    <el-button size="small" type="primary" plain @click="router.push(`/publish/${item.id}`)">编辑</el-button>
                    <el-button size="small" type="danger" plain @click="deleteContent(item.id)">删除</el-button>
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
                <div v-for="item in myDrafts" :key="item.id" class="work-item">
                  <div class="work-cover" v-if="item.coverImage">
                    <img :src="item.coverImage" alt="封面" />
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
                    <el-button size="small" type="primary" plain @click="router.push(`/publish/${item.id}`)">
                      继续编辑
                    </el-button>
                    <el-button size="small" type="danger" plain @click="deleteDraft(item.id)">
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { userApi } from '@/api/user'
import { contentApi } from '@/api/content'
import { uploadApi } from '@/api/upload'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const userInfo = ref<any>(null)
const newAvatar = ref('')
const avatarUploading = ref(false)
const avatarFileInput = ref<HTMLInputElement | null>(null)

const triggerAvatarFile = () => avatarFileInput.value?.click()

const onAvatarFileChange = async (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
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
const activeTab = ref('content')
const myContent = ref<any[]>([])
const contentPage = ref(1)
const contentTotal = ref(0)
const contentLoading = ref(false)

// 草稿
const myDrafts = ref<any[]>([])
const draftsPage = ref(1)
const draftsTotal = ref(0)
const draftsLoading = ref(false)

const formatDate = (d: string) => d ? new Date(d).toLocaleDateString('zh-CN') : ''

const loadUserInfo = async () => {
  const res = await userApi.getInfo()
  userInfo.value = res.data
}

const updateAvatar = async () => {
  if (!newAvatar.value) return
  await userApi.updateInfo({ avatar: newAvatar.value })
  userStore.setUserInfo({ ...userStore.userInfo!, avatar: newAvatar.value })
  userInfo.value.avatar = newAvatar.value
  ElMessage.success('头像已更新')
  newAvatar.value = ''
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
  await ElMessageBox.confirm('确认删除该作品？删除后无法恢复。', '警告', { type: 'warning' })
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
}

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

onMounted(() => {
  loadUserInfo()
  loadMyContent()
})
</script>

<style scoped>
.user-center-page { min-height: calc(100vh + 1px); background: #f5f7fa; }
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
  border-radius: 8px;
  padding: 24px;
  height: fit-content;
}
.avatar-section { text-align: center; margin-bottom: 20px; }
.big-avatar { margin-bottom: 12px; }
.username { font-size: 18px; font-weight: 600; margin: 8px 0; }
.join-date { font-size: 12px; color: #aaa; margin-top: 8px; }
.edit-avatar { padding-top: 16px; border-top: 1px solid #f0f0f0; }
.upload-btn-wrap { margin-bottom: 4px; }
.edit-email { padding-top: 16px; border-top: 1px solid #f0f0f0; margin-top: 16px; }
.email-display { font-size: 13px; min-height: 20px; }
.email-value { color: #333; word-break: break-all; }
.email-empty { color: #bbb; font-style: italic; }
.edit-section { padding-top: 16px; border-top: 1px solid #f0f0f0; margin-top: 16px; }
.section-label { font-size: 13px; color: #999; margin: 0 0 6px; }
.delete-account-section { padding-top: 16px; border-top: 1px solid #f0f0f0; margin-top: 16px; }
.user-content { background: #fff; border-radius: 8px; padding: 24px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }

/* 作品/草稿通用列表 */
.work-list { display: flex; flex-direction: column; gap: 12px; }
.work-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: box-shadow 0.2s;
}
.work-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.08); }

/* 封面缩略图 */
.work-cover {
  width: 44px;
  height: 58px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}
.work-cover img { width: 100%; height: 100%; object-fit: cover; }

.work-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.work-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s;
}
.work-title:hover { color: #409eff; }
.work-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.work-time { font-size: 12px; color: #aaa; }
.work-stat { font-size: 12px; color: #bbb; }
.work-actions { display: flex; gap: 8px; flex-shrink: 0; }
</style>
