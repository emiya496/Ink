<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <el-input v-model="newTagName" placeholder="新增系统标签名" style="width:240px" />
      <el-button type="primary" @click="createTag">新增系统标签</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="tagName" label="标签名" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.type === 'system' ? 'primary' : 'info'">{{ row.type === 'system' ? '系统标签' : '自定义' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '正常' ? 'success' : 'danger'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" :type="row.status === '正常' ? 'warning' : 'success'"
            @click="toggleStatus(row)">{{ row.status === '正常' ? '禁用' : '启用' }}</el-button>
          <el-button size="small" type="danger" @click="deleteTag(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="prev, pager, next, total" @current-change="loadList" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const newTagName = ref('')

const formatDate = (d: string) => d ? new Date(d).toLocaleString('zh-CN') : ''

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminApi.listTags({ page: page.value, size: 20 })
    list.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

const createTag = async () => {
  if (!newTagName.value.trim()) { ElMessage.warning('请输入标签名'); return }
  await adminApi.createTag(newTagName.value.trim())
  ElMessage.success('标签已创建')
  newTagName.value = ''
  loadList()
}

const toggleStatus = async (row: any) => {
  const newStatus = row.status === '正常' ? '禁用' : '正常'
  await adminApi.updateTagStatus(row.id, newStatus)
  row.status = newStatus
  ElMessage.success('状态已更新')
}

const deleteTag = async (id: number) => {
  await ElMessageBox.confirm('确认删除该标签？', '警告', { type: 'warning' })
  await adminApi.deleteTag(id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { background: #fff; border-radius: 8px; padding: 20px; }
.page-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
</style>
