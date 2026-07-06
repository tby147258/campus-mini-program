<template>
  <div>
    <!-- 系统配置 -->
    <el-card style="margin-bottom: 16px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>系统参数配置</span>
          <el-button type="primary" size="small" @click="showAddDialog">新增配置</el-button>
        </div>
      </template>

      <el-table :data="configList" border stripe v-loading="configLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="configKey" label="配置键" width="180" />
        <el-table-column prop="configValue" label="配置值" min-width="200" />
        <el-table-column prop="description" label="说明" min-width="180" />
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 管理员密码 -->
    <el-card>
      <template #header>修改管理员密码</template>
      <el-form :model="pwdForm" label-width="120px" style="max-width: 500px;" :rules="pwdRules" ref="pwdFormRef">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" placeholder="请输入新密码（至少6位）" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="pwdForm.confirm" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="pwdLoading" @click="handleChangePwd">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 新增/编辑配置弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑配置' : '新增配置'" width="550px">
      <el-form :model="configForm" label-width="100px" :rules="configRules" ref="configFormRef">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="configForm.configKey" placeholder="如 site_name" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="configForm.configValue" placeholder="配置值" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="configForm.description" placeholder="配置说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSaveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { configApi, userApi } from '../api'

// ── 系统配置 ──
const configList = ref([])
const configLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saveLoading = ref(false)
const configFormRef = ref(null)
const editId = ref(null)

const configForm = reactive({ configKey: '', configValue: '', description: '' })
const configRules = { configKey: [{ required: true, message: '配置键不能为空' }], configValue: [{ required: true, message: '配置值不能为空' }] }

const loadConfig = async () => {
  configLoading.value = true
  try {
    const res = await configApi.list()
    configList.value = res.data || []
  } finally {
    configLoading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  configForm.configKey = ''
  configForm.configValue = ''
  configForm.description = ''
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  configForm.configKey = row.configKey
  configForm.configValue = row.configValue
  configForm.description = row.description
  dialogVisible.value = true
}

const handleSaveConfig = async () => {
  const valid = await configFormRef.value.validate().catch(() => false)
  if (!valid) return
  saveLoading.value = true
  try {
    if (isEdit.value) {
      await configApi.update(editId.value, configForm)
      ElMessage.success('配置已更新')
    } else {
      await configApi.create(configForm)
      ElMessage.success('配置已新增')
    }
    dialogVisible.value = false
    loadConfig()
  } finally {
    saveLoading.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除配置项 "' + row.configKey + '" 吗？', '确认', { type: 'warning' })
  await configApi.delete(row.id)
  ElMessage.success('已删除')
  loadConfig()
}

// ── 修改密码 ──
const pwdForm = reactive({ password: '', confirm: '' })
const pwdLoading = ref(false)
const pwdFormRef = ref(null)

const pwdRules = {
  password: [{ required: true, message: '密码不能为空' }, { min: 6, message: '密码至少6位' }],
  confirm: [{ required: true, message: '请确认密码' }, { validator: (_, v, cb) => pwdForm.password === v ? cb() : cb(new Error('两次密码不一致')) }]
}

const handleChangePwd = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    await userApi.changePassword(user.id, { password: pwdForm.password })
    ElMessage.success('密码修改成功')
    pwdForm.password = ''
    pwdForm.confirm = ''
  } finally {
    pwdLoading.value = false
  }
}

onMounted(loadConfig)
</script>