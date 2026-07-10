<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in summaryCards" :key="card.label">
        <el-card shadow="hover" style="margin-bottom: 16px; text-align: center;">
          <div style="font-size: 28px; font-weight: bold; color: #409eff;">{{ card.value }}</div>
          <div style="color: #999; margin-top: 8px;">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>欢迎使用校园服务平台管理后台</template>
          <div style="color: #666; line-height: 2;">
            <p>本系统提供公告管理、失物招领管理、报修工单管理、用户管理、系统配置等功能。</p>
            <p>请使用左侧菜单导航，选择需要管理的功能模块。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { statisticsApi } from '../api'

const stats = ref({})

const loadStats = async () => {
  const res = await statisticsApi.get()
  stats.value = res
}

const summaryCards = computed(() => [
  { label: '用户总数', value: stats.value.userCount || 0 },
  { label: '公告总数', value: stats.value.announcementCount || 0 },
  { label: '报修工单', value: stats.value.repairOrderCount || 0 },
  { label: '失物信息', value: stats.value.lostFoundCount || 0 }
])

onMounted(loadStats)
</script>