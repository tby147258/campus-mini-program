<template>
  <el-card>
    <template #header>数据统计</template>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>公告发布统计</template>
          <div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #ccc;">图表区域（待集成ECharts）</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>工单处理统计</template>
          <div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #ccc;">图表区域（待集成ECharts）</div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>