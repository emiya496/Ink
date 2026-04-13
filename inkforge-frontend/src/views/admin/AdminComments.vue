<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索评论内容..."
        prefix-icon="Search"
        clearable
        class="search-input"
        @keyup.enter="doSearch"
        @clear="doSearch"
      />
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="contentId" label="内容ID" width="100">
        <template #default="{ row }">
          <el-link type="primary" @click="goDetail(row.contentId)">{{ row.contentId }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="100">
        <template #default="{ row }">
          <el-link type="primary" @click="goUser(row.userId)">{{ row.userId }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评论内容" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link @click="openPreview(row)">{{ row.content }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="评论时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="deleteComment(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="prev, pager, next, total" @current-change="loadList" />
    </div>

    <!-- 评论详情弹窗 -->
    <el-dialog v-model="previewVisible" title="评论详情" width="480px" destroy-on-close>
      <template v-if="previewItem">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="评论ID">{{ previewItem.id }}</el-descriptions-item>
          <el-descriptions-item label="所属内容">
            <el-link type="primary" @click="goDetail(previewItem.contentId)">ID: {{ previewItem.contentId }}</el-link>
          </el-descriptions-item>
          <el-descriptions-item label="评论用户">
            <el-link type="primary" @click="goUser(previewItem.userId)">ID: {{ previewItem.userId }}</el-link>
          </el-descriptions-item>
          <el-descriptions-item label="评论时间">{{ formatDate(previewItem.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <div class="comment-full-text">{{ previewItem.content }}</div>
      </template>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="danger" @click="deleteFromPreview(previewItem?.id)">删除评论</el-button>
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
    const res = await adminApi.listComments({ page: page.value, size: 20, keyword: keyword.value || undefined })
    list.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

const doSearch = () => { page.value = 1; loadList() }

const openPreview = (row: any) => {
  previewItem.value = row
  previewVisible.value = true
}

const goDetail = (contentId: number) => {
  window.open(router.resolve(`/detail/${contentId}`).href, '_blank')
}

const goUser = (userId: number) => {
  window.open(router.resolve(`/user/${userId}`).href, '_blank')
}

const deleteComment = async (id: number) => {
  await ElMessageBox.confirm('确认删除该评论？', '警告', { type: 'warning' })
  await adminApi.deleteComment(id)
  ElMessage.success('已删除')
  loadList()
}

const deleteFromPreview = async (id: number) => {
  await ElMessageBox.confirm('确认删除该评论？', '警告', { type: 'warning' })
  await adminApi.deleteComment(id)
  ElMessage.success('已删除')
  previewVisible.value = false
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.search-input { width: 260px; }
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
.comment-full-text {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}
</style>
