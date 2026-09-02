<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">角色管理</h1>
      <span class="desc">管理系统角色和权限</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.roleName" placeholder="角色名称" clearable style="width: 180px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="0" />
        <el-option label="停用" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增角色</el-button>
      <span class="total">共 {{ tableData.length }} 个角色</span>
    </div>

    <el-table :data="tableData" row-key="roleId" v-loading="loading">
      <el-table-column prop="roleId" label="角色编号" width="80" />
      <el-table-column prop="roleName" label="角色名称" width="140" />
      <el-table-column prop="roleKey" label="权限标识" width="160" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="0" :inactive-value="1" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="showPermDialog(row)">权限</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="roleForm" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="角色名称" prop="roleName" required>
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="权限标识" prop="roleKey" required>
          <el-input v-model="roleForm.roleKey" placeholder="请输入权限标识" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="roleForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="roleForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配弹窗 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="500px">
      <p style="margin-bottom: 16px; color: #606266;">当前角色：<strong>{{ currentRole?.roleName }}</strong></p>
      <el-tree
        ref="permTreeRef"
        :data="menuTree"
        show-checkbox
        node-key="menuId"
        :default-checked-keys="checkedKeys"
        :props="{ label: 'menuName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePerm" :loading="savingPerm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole as deleteRoleApi, assignRoleMenus } from '@/api/system'
import { getMenuList } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const savingPerm = ref(false)
const tableData = ref([])

const searchForm = reactive({ roleName: '', status: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingRoleId = ref(null)
const formRef = ref(null)

const roleForm = reactive({
  roleName: '',
  roleKey: '',
  sort: 0,
  status: 0,
  remark: ''
})

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
}

// 权限分配
const permDialogVisible = ref(false)
const currentRole = ref(null)
const checkedKeys = ref([])
const permTreeRef = ref(null)
const menuTree = ref([])

onMounted(() => {
  loadData()
  loadMenuTree()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getRoleList()
    let list = res.data || []
    // 前端搜索过滤
    if (searchForm.roleName) {
      list = list.filter(item => item.roleName.includes(searchForm.roleName))
    }
    if (searchForm.status !== '') {
      list = list.filter(item => item.status === searchForm.status)
    }
    tableData.value = list
  } catch (error) {
    console.error('加载角色列表失败', error)
  } finally {
    loading.value = false
  }
}

async function loadMenuTree() {
  try {
    const res = await getMenuList()
    menuTree.value = res.data || []
  } catch (error) {
    console.error('加载菜单列表失败', error)
  }
}

function handleSearch() {
  loadData()
}

function resetSearch() {
  Object.assign(searchForm, { roleName: '', status: '' })
  loadData()
}

function showDialog(row) {
  isEdit.value = !!row
  if (row) {
    editingRoleId.value = row.roleId
    Object.assign(roleForm, {
      roleName: row.roleName,
      roleKey: row.roleKey,
      sort: row.sort,
      status: row.status,
      remark: row.remark || ''
    })
  } else {
    editingRoleId.value = null
    Object.assign(roleForm, { roleName: '', roleKey: '', sort: 0, status: 0, remark: '' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()

  saving.value = true
  try {
    if (isEdit.value) {
      await updateRole({ roleId: editingRoleId.value, ...roleForm })
      ElMessage.success('角色更新成功')
    } else {
      await addRole(roleForm)
      ElMessage.success('角色创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('保存角色失败', error)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除角色"${row.roleName}"吗？`, '警告', { type: 'error' })
    await deleteRoleApi(row.roleId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色失败', error)
    }
  }
}

async function handleStatusChange(row) {
  try {
    await updateRole({ roleId: row.roleId, status: row.status })
    ElMessage.success(`角色已${row.status === 0 ? '启用' : '停用'}`)
  } catch (error) {
    row.status = row.status === 0 ? 1 : 0
    console.error('修改状态失败', error)
  }
}

function showPermDialog(row) {
  currentRole.value = row
  // 设置已选中的菜单ID
  checkedKeys.value = row.menuIds || []
  permDialogVisible.value = true
}

async function handleSavePerm() {
  if (!permTreeRef.value) return

  savingPerm.value = true
  try {
    const checkedKeys = permTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permTreeRef.value.getHalfCheckedKeys()
    const menuIds = [...checkedKeys, ...halfCheckedKeys]

    await assignRoleMenus(currentRole.value.roleId, menuIds)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('分配权限失败', error)
  } finally {
    savingPerm.value = false
  }
}
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
</style>
