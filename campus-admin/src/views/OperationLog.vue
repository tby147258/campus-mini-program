<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span>操作日志</span>
        <el-button size="small" @click="loadData">刷新</el-button>
      </div>
    </template>

    <!-- 搜索栏 -->
    <el-form :inline="true" style="margin-bottom: 16px;">
      <el-form-item label="模块">
        <el-select v-model="query.module" clearable placeholder="全部模块" style="width: 140px;">
          <el-option label="全部" value="" />
          <el-option label="认证" value="auth" />
          <el-option label="公告" value="announcement" />
          <el-option label="失物招领" value="lost_found" />
          <el-option label="报修" value="repair" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作类型">
        <el-select v-model="query.action" clearable placeholder="全部类型" style="width: 140px;">
          <el-option label="全部" value="" />
          <el-option label="登录" value="login" />
          <el-option label="新增" value="create" />
          <el-option label="修改" value="update" />
          <el-option label="删除" value="delete" />
          <el-option label="审核" value="audit" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
          end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 260px;" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 日志表格 -->
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="userId" label="操作人ID" width="90" />
      <el-table-column label="模块" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ moduleMap[row.module] || row.module }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" width="100">
        <template #default="{ row }">
          <el-tag :type="actionTypeMap[row.action]" size="small">{{ actionMap[row.action] || row.action }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="操作描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP地址" width="140" />
      <el-table-column prop="createdAt" label="操作时间" width="170" />
    </el-table>

    <!-- 分页 -->
    <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size"
        :page-sizes="[10, 20, 50, 100]" :total="total" layout="total, sizes, prev, pager, next"
        @size-change="loadData" @current-change="loadData" />
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { logApi } from '../api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dateRange = ref([])

const query = reactive({
  page: 1,
  size: 20,
  module: '',
  action: '',
  startDate: '',
  endDate: ''
})

const moduleMap = { auth: '认证', announcement: '公告', lost_found: '失物招领', repair: '报修' }
const actionMap = { login: '登录', create: '新增', update: '修改', delete: '删除', audit: '审核' }
const actionTypeMap = { login: 'info', create: 'success', update: 'warning', delete: 'danger', audit: 'primary' }

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    } else {
      delete params.startDate
      delete params.endDate
    }
    // 移除空值
    Object.keys(params).forEach(k => {
      if (!params[k] && params[k] !== 0 && params[k] !== false) delete params[k]
    })
    const res = await logApi.page(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.module = ''
  query.action = ''
  query.page = 1
  dateRange.value = []
  loadData()
}

loadData()
</script>