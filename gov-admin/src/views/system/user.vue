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
        <el-option label="正常" :value="0" />
        <el-option label="停用" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showAddDialog">➕ 新增用户</el-button>
      <span class="total">共 {{ total }} 个用户</span>
    </div>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column label="用户信息" min-width="200">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="user-avatar">{{ (row.nickName || row.userName || '').charAt(0) }}</div>
            <div>
              <div class="user-name">{{ row.nickName }}</div>
              <div class="user-account">{{ row.userName }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="deptName" label="所属部门" width="140" />
      <el-table-column label="角色" width="140">
        <template #default="{ row }">
          <el-tag v-for="r in (row.roles || [])" :key="r.roleId" type="primary" size="small" effect="plain" class="role-tag">{{ r.roleName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="0" :inactive-value="1" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="resetPassword(row)">重置密码</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ total }} 条记录，每页 {{ pageSize }} 条</span>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        layout="prev, pager, next"
        :total="total"
        @current-change="loadData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="600px">
      <el-form :model="userForm" :rules="formRules" ref="formRef" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="userName" required>
              <el-input v-model="userForm.userName" placeholder="请输入用户名" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="nickName" required>
              <el-input v-model="userForm.nickName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone" required>
              <el-input v-model="userForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId" required>
              <el-tree-select
                v-model="userForm.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'deptId' }"
                placeholder="请选择部门"
                style="width: 100%"
                check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="roleIds" required>
              <el-select v-model="userForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
                <el-option v-for="role in roleList" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="userForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, addUser, updateUser, deleteUser as deleteUserApi, resetUserPassword, assignUserRoles } from '@/api/system'
import { getRoleList } from '@/api/system'
import { getDeptList } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ username: '', phone: '', status: '' })

const deptTree = ref([])
const roleList = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingUserId = ref(null)
const formRef = ref(null)

const userForm = reactive({
  userName: '',
  nickName: '',
  phone: '',
  email: '',
  deptId: '',
  roleIds: [],
  remark: ''
})

const formRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  roleIds: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

onMounted(() => {
  loadData()
  loadDeptTree()
  loadRoleList()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getUserPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载用户列表失败', error)
  } finally {
    loading.value = false
  }
}

async function loadDeptTree() {
  try {
    const res = await getDeptList()
    deptTree.value = res.data || []
  } catch (error) {
    console.error('加载部门列表失败', error)
  }
}

async function loadRoleList() {
  try {
    const res = await getRoleList()
    roleList.value = res.data || []
  } catch (error) {
    console.error('加载角色列表失败', error)
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  Object.assign(searchForm, { username: '', phone: '', status: '' })
  handleSearch()
}

function showAddDialog() {
  isEdit.value = false
  editingUserId.value = null
  Object.assign(userForm, { userName: '', nickName: '', phone: '', email: '', deptId: '', roleIds: [], remark: '' })
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  editingUserId.value = row.userId
  Object.assign(userForm, {
    userName: row.userName,
    nickName: row.nickName,
    phone: row.phone,
    email: row.email || '',
    deptId: row.deptId,
    roleIds: (row.roles || []).map(r => r.roleId),
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function saveUser() {
  if (!formRef.value) return
  await formRef.value.validate()

  saving.value = true
  try {
    if (isEdit.value) {
      await updateUser({ userId: editingUserId.value, ...userForm })
      ElMessage.success('用户更新成功')
    } else {
      await addUser(userForm)
      ElMessage.success('用户创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存用户失败', error)
  } finally {
    saving.value = false
  }
}

async function handleStatusChange(row) {
  try {
    await updateUser({ userId: row.userId, status: row.status })
    ElMessage.success(`用户已${row.status === 0 ? '启用' : '停用'}`)
  } catch (error) {
    row.status = row.status === 0 ? 1 : 0
    console.error('修改状态失败', error)
  }
}

async function resetPassword(row) {
  try {
    await ElMessageBox.confirm(`确定要重置"${row.nickName}"的密码吗？`, '提示', { type: 'warning' })
    await resetUserPassword(row.userId)
    ElMessage.success('密码已重置为：123456')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败', error)
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.nickName}"吗？删除后不可恢复。`, '警告', { type: 'error' })
    await deleteUserApi(row.userId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败', error)
    }
  }
}
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
