<template>
  <el-container style="height: 100vh">
    <el-aside width="220px">
      <el-menu router :default-active="route.path" background-color="#304156" text-color="#fff" active-text-color="#409EFF">
        <div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 16px; font-weight: bold; border-bottom: 1px solid #1f2d3d;">
          校园管理后台
        </div>
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>仪表盘</span></el-menu-item>
        <el-sub-menu index="announcement">
          <template #title><el-icon><Document /></el-icon><span>公告管理</span></template>
          <el-menu-item index="/announcements">公告列表</el-menu-item>
          <el-menu-item index="/announcements/create">发布公告</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/lost-found"><el-icon><Search /></el-icon><span>失物招领管理</span></el-menu-item>
        <el-sub-menu index="repair">
          <template #title><el-icon><Tools /></el-icon><span>报修工单管理</span></template>
          <el-menu-item index="/repairs">工单列表</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/operation-logs"><el-icon><List /></el-icon><span>操作日志</span></el-menu-item>
        <el-menu-item index="/statistics"><el-icon><PieChart /></el-icon><span>数据统计</span></el-menu-item>
        <el-menu-item index="/settings"><el-icon><Setting /></el-icon><span>系统设置</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between; padding: 0 20px;">
        <span>{{ route.meta.title }}</span>
        <el-button type="danger" size="small" @click="logout">退出登录</el-button>
      </el-header>
      <el-main style="background: #f0f2f5;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()
const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>