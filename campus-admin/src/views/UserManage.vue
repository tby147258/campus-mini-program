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
    </el-table>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
const list = ref([])
</script>