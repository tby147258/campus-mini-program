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
import { ref, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

// 概览数据（可用实际API替换）
const summaryCards = ref([
  { label: '用户总数', value: 5 },
  { label: '公告总数', value: 8 },
  { label: '报修工单', value: 8 },
  { label: '失物信息', value: 8 }
])

// 公告柱状图
const announcementChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: ['教务通知', '活动通知', '紧急通知'] },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: [4, 3, 1], itemStyle: { color: '#409eff' } }],
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
}))

// 工单饼图
const repairChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: [
      { value: 3, name: '待处理', itemStyle: { color: '#f56c6c' } },
      { value: 2, name: '处理中', itemStyle: { color: '#e6a23c' } },
      { value: 3, name: '已完成', itemStyle: { color: '#67c23a' } }
    ],
    label: { formatter: '{b}: {c}' }
  }]
}))

// 失物招领饼图
const lostFoundChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    data: [
      { value: 6, name: '已发布', itemStyle: { color: '#67c23a' } },
      { value: 2, name: '待审核', itemStyle: { color: '#e6a23c' } },
      { value: 1, name: '未通过', itemStyle: { color: '#f56c6c' } }
    ],
    label: { formatter: '{b}: {c}' }
  }]
}))

// 用户角色分布
const userChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    data: [
      { value: 1, name: '管理员', itemStyle: { color: '#409eff' } },
      { value: 4, name: '学生', itemStyle: { color: '#67c23a' } }
    ],
    label: { formatter: '{b}: {c}' }
  }]
}))
</script>