<template>
  <el-card>
    <template #header>用户管理</template>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="nickname" label="昵称" width="150" />
      <el-table-column prop="studentNo" label="学号" width="130" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="角色" width="80">
        <template #default="{ row }">{{ row.role === 1 ? '管理员' : '学生' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleChangePassword(row)">改密</el-button>
          <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(row)">
            <template #reference>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../api'

const list = ref([])

const loadList = async () => {
  const res = await userApi.page({ page: 1, size: 100 })
  list.value = res.records || []
}

const handleDelete = async (row) => {
  try {
    await userApi.del(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    ElMessage.error(e.msg || e.message || '删除失败')
  }
}

const handleChangePassword = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码', '修改密码', {
      inputType: 'password',
      inputValidator: (v) => v && v.length >= 6 ? true : '密码至少6位'
    })
    if (!value) return
    await userApi.changePassword(row.id, { password: value })
    ElMessage.success('密码修改成功')
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.msg || e.message || '操作失败')
    }
  }
}

onMounted(loadList)
</script>