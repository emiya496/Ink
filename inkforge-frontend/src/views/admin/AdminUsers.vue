<template>
  <div class="admin-page">
    <div class="page-toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名" prefix-icon="Search" @keyup.enter="loadList" clearable style="width:240px" />
      <el-button type="primary" @click="loadList">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
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
const keyword = ref('')

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
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
</style>
