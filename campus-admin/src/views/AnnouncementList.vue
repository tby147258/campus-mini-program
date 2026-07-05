<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>公告列表</span>
          <el-button type="primary" @click="$router.push('/announcements/create')">发布公告</el-button>
        </div>
      </template>
      <el-table :data="list" border stripe style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="viewCount" label="浏览量" width="80" />
        <el-table-column prop="createdAt" label="发布时间" width="160" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/announcements/edit/${row.id}`)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { announcementApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loadList = async () => {
  const res = await announcementApi.list({ page: 1, size: 100 })
  list.value = res.data.records || []
}
const handleDelete = async (id) => {
  await ElMessageBox.confirm('确认删除？')
  await announcementApi.delete(id)
  ElMessage.success('已删除')
  loadList()
}
onMounted(loadList)
</script>