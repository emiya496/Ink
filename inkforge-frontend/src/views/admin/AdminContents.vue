<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索标题..."
        prefix-icon="Search"
        clearable
        class="search-input"
        @keyup.enter="doSearch"
        @clear="doSearch"
      />
      <el-select v-model="filterType" placeholder="全部类型" clearable style="width:150px" @change="doSearch">
        <el-option v-for="t in types" :key="t" :label="t" :value="t" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:140px" @change="doSearch">
        <el-option label="正常" value="正常" />
        <el-option label="草稿" value="草稿" />
        <el-option label="下架" value="下架" />
      </el-select>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link type="primary" @click="openPreview(row)">{{ row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '正常' ? 'success' : row.status === '下架' ? 'danger' : 'info'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="作者ID" width="100" />
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="createTime" label="发布时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status !== '草稿'" size="small" :type="row.status === '下架' ? 'success' : 'warning'"
            @click="toggleStatus(row)">{{ row.status === '下架' ? '恢复' : '下架' }}</el-button>
          <el-button v-else size="small" style="visibility:hidden">下架</el-button>
          <el-button size="small" type="danger" @click="deleteContent(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" @current-change="loadList" />
    </div>

    <!-- 内容预览弹窗 -->
    <el-dialog v-model="previewVisible" title="内容预览" width="640px" destroy-on-close>
      <template v-if="previewItem">
        <div class="preview-header">
          <img v-if="previewItem.coverImages?.[0]" :src="previewItem.coverImages[0]" class="preview-cover" />
          <div class="preview-meta">
            <h3 class="preview-title">{{ previewItem.title }}</h3>
            <div class="preview-tags">
              <el-tag size="small">{{ previewItem.type }}</el-tag>
              <el-tag :type="previewItem.status === '正常' ? 'success' : previewItem.status === '下架' ? 'danger' : 'info'" size="small">{{ previewItem.status }}</el-tag>
              <el-tag type="info" size="small">{{ visibilityLabel(previewItem.visibility) }}</el-tag>
            </div>
            <div class="preview-info">
              <span>作者ID：<el-link type="primary" @click="goUserProfile(previewItem.userId)">{{ previewItem.userId }}</el-link></span>
              <span>浏览：{{ previewItem.viewCount }}</span>
              <span>点赞：{{ previewItem.likeCount }}</span>
            </div>
            <div class="preview-info">{{ formatDate(previewItem.createTime) }}</div>
          </div>
        </div>
        <el-divider />
        <div class="preview-body">{{ previewItem.body?.slice(0, 800) }}{{ (previewItem.body?.length > 800) ? '…' : '' }}</div>
      </template>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="goDetail(previewItem?.id)">查看详情页</el-button>
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
const types = ['小说', '散文', '诗词', '随笔', '名人名言', '杂谈']
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const filterType = ref('')
const filterStatus = ref('')
const keyword = ref('')
const previewVisible = ref(false)
const previewItem = ref<any>(null)

const formatDate = (d: string) => d ? new Date(d).toLocaleString('zh-CN') : ''
const visibilityLabel = (v: string) => ({ public: '公开', private: '私密', followers_only: '仅粉丝' }[v] ?? '公开')

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminApi.listContents({
      page: page.value, size: 10,
      type: filterType.value || undefined,
      status: filterStatus.value || undefined,
      keyword: keyword.value || undefined
    })
    list.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

const doSearch = () => { page.value = 1; loadList() }

const openPreview = (row: any) => {
  previewItem.value = row
  previewVisible.value = true
}

const goDetail = (id: number) => {
  window.open(router.resolve(`/detail/${id}`).href, '_blank')
}

const goUserProfile = (userId: number) => {
  window.open(router.resolve(`/user/${userId}`).href, '_blank')
}

const toggleStatus = async (row: any) => {
  const newStatus = row.status === '下架' ? '正常' : '下架'
  await adminApi.updateContentStatus(row.id, newStatus)
  row.status = newStatus
  ElMessage.success('状态已更新')
}

const deleteContent = async (id: number) => {
  await ElMessageBox.confirm('确认删除该内容？', '警告', { type: 'warning' })
  await adminApi.deleteContent(id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }
.search-input { width: 220px; }
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
.preview-header { display: flex; gap: 16px; }
.preview-cover { width: 120px; height: 80px; object-fit: cover; border-radius: 6px; flex-shrink: 0; }
.preview-meta { flex: 1; min-width: 0; }
.preview-title { margin: 0 0 8px; font-size: 16px; font-weight: 600; }
.preview-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px; }
.preview-info { font-size: 13px; color: #666; display: flex; gap: 12px; margin-bottom: 4px; }
.preview-body { font-size: 14px; color: #444; line-height: 1.8; max-height: 260px; overflow-y: auto; white-space: pre-wrap; word-break: break-all; }
</style>
