<template>
  <el-card>
    <template #header>系统设置</template>
    <el-form label-width="120px" style="max-width: 500px;">
      <el-form-item label="管理员密码">
        <el-input type="password" placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input type="password" placeholder="请再次输入新密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary">保存修改</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>