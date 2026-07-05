<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in stats" :key="item.label">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <div style="font-size: 28px; font-weight: bold; color: #409EFF;">{{ item.value }}</div>
            <div style="font-size: 14px; color: #999; margin-top: 8px;">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 20px;">
      <template #header>系统概览</template>
      <p>欢迎使用校园综合服务平台管理后台</p>
      <p style="margin-top: 10px; color: #666;">功能模块：公告管理 | 失物招领管理 | 报修工单管理 | 用户管理 | 数据统计</p>
    </el-card>
  </div>
</template>

<script setup>
const stats = [
  { label: '公告总数', value: 0 },
  { label: '待审核信息', value: 0 },
  { label: '待处理工单', value: 0 },
  { label: '注册用户', value: 0 },
]
</script>