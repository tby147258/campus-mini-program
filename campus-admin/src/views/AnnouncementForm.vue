<template>
  <el-card>
    <template #header>{{ isEdit ? '编辑公告' : '发布公告' }}</template>
    <el-form :model="form" label-width="80px" style="max-width: 800px;">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入公告标题" />
      </el-form-item>
      <el-form-item label="类别">
        <el-select v-model="form.category">
          <el-option label="教务通知" value="教务通知" />
          <el-option label="活动通知" value="活动通知" />
          <el-option label="紧急通知" value="紧急通知" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入公告内容" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">发布</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { announcementApi } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const form = ref({ title: '', category: '教务通知', content: '' })

onMounted(async () => {
  if (isEdit.value) {
    const res = await announcementApi.getById(route.params.id)
    form.value = res.data
  }
})

const handleSubmit = async () => {
  if (isEdit.value) {
    await announcementApi.update(route.params.id, form.value)
  } else {
    await announcementApi.create(form.value)
  }
  ElMessage.success(isEdit.value ? '编辑成功' : '发布成功')
  router.push('/announcements')
}
</script>