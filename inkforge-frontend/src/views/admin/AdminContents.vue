<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <el-select v-model="filterType" placeholder="全部类型" clearable style="width:150px" @change="loadList">
        <el-option v-for="t in types" :key="t" :label="t" :value="t" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:140px" @change="loadList">
        <el-option label="正常" value="正常" />
        <el-option label="审核中" value="审核中" />
        <el-option label="下架" value="下架" />
      </el-select>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '正常' ? 'success' : row.status === '下架' ? 'danger' : 'warning'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="作者ID" width="100" />
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="createTime" label="发布时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" :type="row.status === '下架' ? 'success' : 'warning'"
            @click="toggleStatus(row)">{{ row.status === '下架' ? '恢复' : '下架' }}</el-button>
          <el-button size="small" type="danger" @click="deleteContent(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next, total" @current-change="loadList" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const types = ['小说', '散文', '诗词', '随笔', '名人名言', '杂谈']
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const filterType = ref('')
const filterStatus = ref('')

const formatDate = (d: string) => d ? new Date(d).toLocaleString('zh-CN') : ''

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminApi.listContents({
      page: page.value, size: 10,
      type: filterType.value || undefined,
      status: filterStatus.value || undefined
    })
    list.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
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
.page-toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
</style>
