<template>
  <el-card>
    <template #header>报修工单管理</template>
    <el-table :data="list" border stripe>
      <el-table-column prop="orderNo" label="工单编号" width="160" />
      <el-table-column prop="repairType" label="报修类型" width="80" />
      <el-table-column label="报修地点" width="160">
        <template #default="{ row }">{{ row.campus }}-{{ row.building }}-{{ row.room }}</template>
      </el-table-column>
      <el-table-column prop="description" label="故障描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="160" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" v-if="row.status === 0" @click="handleStatus(row.id, 1)">受理</el-button>
          <el-button size="small" v-if="row.status === 1" type="success" @click="handleComplete(row)">完成</el-button>
          <el-button size="small" v-if="row.status === 0" type="warning" @click="handleReject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { repairApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const statusMap = { 0: { label: '待处理', type: 'warning' }, 1: { label: '处理中', type: 'primary' }, 2: { label: '已完成', type: 'success' }, 3: { label: '已驳回', type: 'danger' } }

const loadList = async () => {
  const res = await repairApi.list({ page: 1, size: 100 })
  list.value = res.records || []
}

const handleStatus = async (id, status) => {
  await repairApi.updateStatus(id, { status })
  ElMessage.success('操作成功')
  loadList()
}

const handleComplete = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入处理结果')
    if (!value) return
    await repairApi.updateStatus(row.id, { status: 2, handleResult: value })
    ElMessage.success('已完成')
    loadList()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.msg || e.message || '操作失败')
    }
  }
}

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因')
    if (!value) return
    await repairApi.updateStatus(row.id, { status: 3, rejectReason: value })
    ElMessage.success('已驳回')
    loadList()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.msg || e.message || '操作失败')
    }
  }
}

onMounted(loadList)
</script>