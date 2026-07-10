<template>
  <div>
    <el-row :gutter="16">
      <!-- 概览卡片 -->
      <el-col :span="6" v-for="card in summaryCards" :key="card.label">
        <el-card shadow="hover" style="margin-bottom: 16px; text-align: center;">
          <div style="font-size: 28px; font-weight: bold; color: #409eff;">{{ card.value }}</div>
          <div style="color: #999; margin-top: 8px;">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="hover" style="margin-bottom: 16px;">
          <template #header>公告发布统计</template>
          <v-chart :option="announcementChartOption" style="height: 320px;" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" style="margin-bottom: 16px;">
          <template #header>工单处理统计</template>
          <v-chart :option="repairChartOption" style="height: 320px;" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="hover" style="margin-bottom: 16px;">
          <template #header>失物招领状态</template>
          <v-chart :option="lostFoundChartOption" style="height: 320px;" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" style="margin-bottom: 16px;">
          <template #header>用户角色分布</template>
          <v-chart :option="userChartOption" style="height: 320px;" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { statisticsApi } from '../api'

use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

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

const announcementChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: ['教务通知', '活动通知', '紧急通知'] },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: [stats.value.announcementCount || 0], itemStyle: { color: '#409eff' } }],
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
}))

const repairChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: [
      { value: stats.value.repairPending || 0, name: '待处理', itemStyle: { color: '#f56c6c' } },
      { value: stats.value.repairProcessing || 0, name: '处理中', itemStyle: { color: '#e6a23c' } },
      { value: stats.value.repairCompleted || 0, name: '已完成', itemStyle: { color: '#67c23a' } },
      { value: stats.value.repairRejected || 0, name: '已驳回', itemStyle: { color: '#909399' } }
    ],
    label: { formatter: '{b}: {c}' }
  }]
}))

const lostFoundChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: [
      { value: stats.value.lostFoundPublished || 0, name: '已发布', itemStyle: { color: '#67c23a' } },
      { value: stats.value.lostFoundPending || 0, name: '待审核', itemStyle: { color: '#e6a23c' } },
      { value: stats.value.lostFoundRejected || 0, name: '未通过', itemStyle: { color: '#f56c6c' } },
      { value: stats.value.lostFoundClosed || 0, name: '已结束', itemStyle: { color: '#909399' } }
    ],
    label: { formatter: '{b}: {c}' }
  }]
}))

const userChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    data: [
      { value: stats.value.adminCount || 0, name: '管理员', itemStyle: { color: '#409eff' } },
      { value: stats.value.studentCount || 0, name: '学生', itemStyle: { color: '#67c23a' } }
    ],
    label: { formatter: '{b}: {c}' }
  }]
}))

onMounted(loadStats)
</script>