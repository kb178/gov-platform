<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">用户管理</h1>
      <span class="desc">管理系统工作人员账号</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.username" placeholder="用户名" clearable style="width: 160px" />
      <el-input v-model="searchForm.phone" placeholder="手机号" clearable style="width: 160px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-select v-model="searchForm.dept" placeholder="部门" clearable style="width: 160px">
        <el-option label="公安局" value="公安局" />
        <el-option label="市场监管局" value="市场监管局" />
        <el-option label="人社局" value="人社局" />
        <el-option label="政务服务中心" value="政务服务中心" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showAddDialog">➕ 新增用户</el-button>
      <el-button @click="ElMessage.info('导出')">📥 导出</el-button>
      <span class="total">共 25 个用户</span>
    </div>

    <el-table :data="tableData">
      <el-table-column label="用户信息" min-width="200">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="user-avatar">{{ row.nickname.charAt(0) }}</div>
            <div>
              <div class="user-name">{{ row.nickname }}</div>
              <div class="user-account">{{ row.username }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="dept" label="所属部门" width="140" />
      <el-table-column label="角色" width="140">
        <template #default="{ row }">
          <el-tag v-for="r in row.roles" :key="r" type="primary" size="small" effect="plain" class="role-tag">{{ r }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="120" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="resetPassword(row)">重置密码</el-button>
          <el-button type="danger" link size="small" @click="deleteUser(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 25 条记录，每页 10 条</span>
      <el-pagination layout="prev, pager, next" :total="25" :page-size="10" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="600px">
      <el-form :model="userForm" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" required>
              <el-input v-model="userForm.username" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="userForm.nickname" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" required>
              <el-input v-model="userForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" required>
              <el-select v-model="userForm.dept" placeholder="请选择部门" style="width: 100%">
                <el-option label="公安局" value="公安局" />
                <el-option label="市场监管局" value="市场监管局" />
                <el-option label="人社局" value="人社局" />
                <el-option label="政务服务中心" value="政务服务中心" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" required>
              <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
                <el-option label="超级管理员" value="超级管理员" />
                <el-option label="局领导" value="局领导" />
                <el-option label="科室负责人" value="科室负责人" />
                <el-option label="审批人员" value="审批人员" />
                <el-option label="窗口人员" value="窗口人员" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="userForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ username: '', phone: '', status: '', dept: '' })

const tableData = ref([
  { nickname: '超级管理员', username: 'admin', dept: '系统管理', roles: ['超级管理员'], phone: '138****8888', status: 1, createTime: '2024-01-01' },
  { nickname: '张科长', username: 'zhangke', dept: '公安局', roles: ['科室负责人'], phone: '139****6666', status: 1, createTime: '2024-01-05' },
  { nickname: '李窗口', username: 'lichuang', dept: '政务服务中心', roles: ['窗口人员'], phone: '137****5555', status: 1, createTime: '2024-01-08' },
  { nickname: '王审批', username: 'wangshenpi', dept: '市场监管局', roles: ['审批人员'], phone: '136****4444', status: 0, createTime: '2024-01-10' },
  { nickname: '刘领导', username: 'liulingdao', dept: '人社局', roles: ['局领导'], phone: '135****3333', status: 1, createTime: '2024-01-12' }
])

const dialogVisible = ref(false)
const isEdit = ref(false)
const userForm = reactive({ username: '', nickname: '', phone: '', email: '', dept: '', role: '', remark: '' })

function showAddDialog() {
  isEdit.value = false
  Object.assign(userForm, { username: '', nickname: '', phone: '', email: '', dept: '', role: '', remark: '' })
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  Object.assign(userForm, { username: row.username, nickname: row.nickname, phone: row.phone, email: '', dept: row.dept, role: row.roles[0], remark: '' })
  dialogVisible.value = true
}

function saveUser() {
  ElMessage.success(isEdit.value ? '用户更新成功' : '用户创建成功')
  dialogVisible.value = false
}

function handleStatusChange(row) { ElMessage.success(`用户已${row.status ? '启用' : '停用'}`) }

function resetPassword(row) {
  ElMessageBox.confirm(`确定要重置"${row.nickname}"的密码吗？`, '提示', { type: 'warning' }).then(() => {
    ElMessage.success('密码已重置为：123456')
  }).catch(() => {})
}

function deleteUser(row) {
  ElMessageBox.confirm(`确定要删除用户"${row.nickname}"吗？删除后不可恢复。`, '警告', { type: 'error' }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { username: '', phone: '', status: '', dept: '' }) }
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar { width: 36px; height: 36px; border-radius: 50%; background: #DBEAFE; display: flex; align-items: center; justify-content: center; font-size: 14px; color: #1E40AF; font-weight: 600; flex-shrink: 0; }
.user-name { font-weight: 500; font-size: 14px; }
.user-account { font-size: 12px; color: #909399; }
.role-tag { margin-right: 4px; margin-bottom: 4px; }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
</style>
