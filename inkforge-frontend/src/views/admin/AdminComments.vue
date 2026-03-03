<template>
  <div class="admin-page">
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="contentId" label="内容ID" width="100" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="content" label="评论内容" show-overflow-tooltip />
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

const formatDate = (d: string) => d ? new Date(d).toLocaleString('zh-CN') : ''

const loadList = async () => {
  loading.value = true
  try {
    const res = await adminApi.listComments({ page: page.value, size: 20 })
    list.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

const deleteComment = async (id: number) => {
  await ElMessageBox.confirm('确认删除该评论？', '警告', { type: 'warning' })
  await adminApi.deleteComment(id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.admin-page { background: #fff; border-radius: 8px; padding: 20px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
</style>
