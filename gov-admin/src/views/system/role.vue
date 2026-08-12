<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">角色管理</h1>
      <span class="desc">管理系统角色和权限</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.roleName" placeholder="角色名称" clearable style="width: 180px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增角色</el-button>
      <span class="total">共 {{ tableData.length }} 个角色</span>
    </div>

    <el-table :data="tableData" row-key="roleId">
      <el-table-column prop="roleId" label="角色编号" width="80" />
      <el-table-column prop="roleName" label="角色名称" width="140" />
      <el-table-column prop="roleKey" label="权限标识" width="160" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="120" />
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
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色名称" required>
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="权限标识" required>
          <el-input v-model="roleForm.roleKey" placeholder="请输入权限标识" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="roleForm.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="roleForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
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
        <el-button type="primary" @click="handleSavePerm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ roleName: '', status: '' })

const tableData = ref([
  { roleId: 1, roleName: '超级管理员', roleKey: 'admin', sort: 1, status: 1, remark: '拥有所有权限', createTime: '2024-01-01' },
  { roleId: 2, roleName: '局领导', roleKey: 'leader', sort: 2, status: 1, remark: '可查看所有数据和审批', createTime: '2024-01-01' },
  { roleId: 3, roleName: '科室负责人', roleKey: 'dept_leader', sort: 3, status: 1, remark: '管理本科室业务', createTime: '2024-01-05' },
  { roleId: 4, roleName: '审批人员', roleKey: 'approver', sort: 4, status: 1, remark: '处理审批业务', createTime: '2024-01-05' },
  { roleId: 5, roleName: '窗口人员', roleKey: 'window', sort: 5, status: 1, remark: '前台受理业务', createTime: '2024-01-08' }
])

const dialogVisible = ref(false)
const isEdit = ref(false)
const roleForm = reactive({ roleName: '', roleKey: '', sort: 0, status: 1, remark: '' })

function showDialog(row) {
  isEdit.value = !!row
  if (row) {
    Object.assign(roleForm, { roleName: row.roleName, roleKey: row.roleKey, sort: row.sort, status: row.status, remark: row.remark })
  } else {
    Object.assign(roleForm, { roleName: '', roleKey: '', sort: 0, status: 1, remark: '' })
  }
  dialogVisible.value = true
}

function handleSave() {
  if (!roleForm.roleName) return ElMessage.warning('请输入角色名称')
  ElMessage.success(isEdit.value ? '角色更新成功' : '角色创建成功')
  dialogVisible.value = false
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除角色"${row.roleName}"吗？`, '警告', { type: 'error' }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleStatusChange(row) {
  ElMessage.success(`角色已${row.status ? '启用' : '停用'}`)
}

// 权限分配
const permDialogVisible = ref(false)
const currentRole = ref(null)
const checkedKeys = ref([1, 2, 3, 4, 5, 6, 7, 8, 9, 10])

const menuTree = ref([
  { menuId: 1, menuName: '工作台', children: [] },
  { menuId: 2, menuName: '审批管理', children: [
    { menuId: 21, menuName: '待我审批' },
    { menuId: 22, menuName: '已办事项' },
    { menuId: 23, menuName: '流程监控' }
  ]},
  { menuId: 3, menuName: '事项管理', children: [
    { menuId: 31, menuName: '事项分类' },
    { menuId: 32, menuName: '事项列表' },
    { menuId: 33, menuName: '办事指南' },
    { menuId: 34, menuName: '表单模板' }
  ]},
  { menuId: 4, menuName: '系统管理', children: [
    { menuId: 41, menuName: '用户管理' },
    { menuId: 42, menuName: '角色管理' },
    { menuId: 43, menuName: '部门管理' },
    { menuId: 44, menuName: '菜单管理' }
  ]}
])

function showPermDialog(row) {
  currentRole.value = row
  permDialogVisible.value = true
}

function handleSavePerm() {
  ElMessage.success('权限分配成功')
  permDialogVisible.value = false
}

function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { roleName: '', status: '' }) }
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
</style>
