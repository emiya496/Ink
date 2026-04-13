<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名..."
        prefix-icon="Search"
        clearable
        class="search-input"
        @keyup.enter="doSearch"
        @clear="doSearch"
      />
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名">
        <template #default="{ row }">
          <el-link type="primary" @click="openPreview(row)">{{ row.username }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '正常' ? 'success' : 'danger'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="row.role !== 'admin'">
            <el-button
              size="small"
              :type="row.status === '正常' ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >{{ row.status === '正常' ? '禁用' : '启用' }}</el-button>
            <el-button size="small" type="danger" @click="deleteUser(row.id)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" @current-change="loadList" />
    </div>

    <!-- 用户预览弹窗 -->
    <el-dialog v-model="previewVisible" title="用户信息" width="480px" destroy-on-close>
      <template v-if="previewItem">
        <div class="user-preview">
          <el-avatar :src="previewItem.avatar" :size="64" class="preview-avatar">
            {{ previewItem.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div class="user-preview-info">
            <div class="user-preview-name">{{ previewItem.username }}</div>
            <div class="user-preview-row">
              <el-tag :type="previewItem.role === 'admin' ? 'danger' : 'primary'" size="small">{{ previewItem.role }}</el-tag>
              <el-tag :type="previewItem.status === '正常' ? 'success' : 'danger'" size="small">{{ previewItem.status }}</el-tag>
            </div>
          </div>
        </div>
        <el-divider />
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="用户ID">{{ previewItem.id }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ previewItem.email || '未绑定' }}</el-descriptions-item>
          <el-descriptions-item label="个人简介">{{ previewItem.bio || '暂无简介' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDate(previewItem.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="goUserProfile(previewItem?.id)">查看主页</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const router = useRouter()
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const keyword = ref('')
const previewVisible = ref(false)
const previewItem = ref<any>(null)

const formatDate = (d: string) => d ? new Date(d).toLocaleString('zh-CN') : ''

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminApi.listUsers({ page: page.value, size: 10, keyword: keyword.value || undefined })
    list.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const doSearch = () => { page.value = 1; loadList() }

const openPreview = (row: any) => {
  previewItem.value = row
  previewVisible.value = true
}

const goUserProfile = (id: number) => {
  window.open(router.resolve(`/user/${id}`).href, '_blank')
}

const toggleStatus = async (row: any) => {
  const newStatus = row.status === '正常' ? '禁用' : '正常'
  await adminApi.updateUserStatus(row.id, newStatus)
  row.status = newStatus
  ElMessage.success(`已${newStatus === '禁用' ? '禁用' : '启用'}用户`)
}

const deleteUser = async (id: number) => {
  await ElMessageBox.confirm('确认删除该用户？', '警告', { type: 'warning' })
  await adminApi.deleteUser(id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.search-input { width: 240px; }
:deep(.search-input .el-input__wrapper) {
  border-radius: 20px;
  background: #f5f7fa;
  box-shadow: none !important;
  border: 1px solid transparent;
  transition: border-color 0.2s, background 0.2s;
}
:deep(.search-input .el-input__wrapper:hover),
:deep(.search-input .el-input__wrapper.is-focus) {
  background: #fff;
  border-color: #409eff;
}
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
.user-preview { display: flex; align-items: center; gap: 16px; }
.preview-avatar { flex-shrink: 0; }
.user-preview-info { flex: 1; }
.user-preview-name { font-size: 18px; font-weight: 600; margin-bottom: 8px; }
.user-preview-row { display: flex; gap: 8px; }
</style>
