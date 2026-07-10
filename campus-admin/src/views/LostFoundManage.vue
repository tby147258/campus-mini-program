<template>
  <div>
    <el-card>
      <template #header>失物招领管理</template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审核" name="pending">
          <el-table :data="pendingList" border stripe>
            <el-table-column prop="itemName" label="物品名称" />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">{{ row.type === 0 ? '失物招领' : '寻物启事' }}</template>
            </el-table-column>
            <el-table-column prop="location" label="地点" width="150" />
            <el-table-column prop="createdAt" label="发布时间" width="160" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" type="success" @click="audit(row.id, 1)">通过</el-button>
                <el-button size="small" type="warning" @click="audit(row.id, 2)">驳回</el-button>
                <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="已发布" name="published">
          <el-table :data="publishedList" border stripe>
            <el-table-column prop="itemName" label="物品名称" />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">{{ row.type === 0 ? '失物招领' : '寻物启事' }}</template>
            </el-table-column>
            <el-table-column prop="location" label="地点" width="150" />
            <el-table-column prop="createdAt" label="发布时间" width="160" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { lostFoundApi } from '../api'
import { ElMessage } from 'element-plus'

const activeTab = ref('pending')
const pendingList = ref([])
const publishedList = ref([])

const loadData = async () => {
  const p1 = lostFoundApi.list({ page: 1, size: 100, status: 0 })
  const p2 = lostFoundApi.list({ page: 1, size: 100, status: 1 })
  const [r1, r2] = await Promise.all([p1, p2])
  pendingList.value = r1.records || []
  publishedList.value = r2.records || []
}

const audit = async (id, status) => {
  await lostFoundApi.audit(id, { status })
  ElMessage.success(status === 1 ? '已通过' : '已驳回')
  loadData()
}

const handleDelete = async (id) => {
  await lostFoundApi.delete(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>