<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">菜单管理</h1>
      <span class="desc">管理系统菜单和权限标识</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.menuName" placeholder="菜单名称" clearable style="width: 180px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增菜单</el-button>
      <el-button @click="toggleExpand">{{ isExpand ? '折叠全部' : '展开全部' }}</el-button>
    </div>

    <el-table :data="tableData" row-key="menuId" :default-expand-all="isExpand" :tree-props="{ children: 'children' }">
      <el-table-column prop="menuName" label="菜单名称" min-width="200" />
      <el-table-column prop="icon" label="图标" width="80">
        <template #default="{ row }">
          <span>{{ row.icon }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column prop="perms" label="权限标识" width="160" show-overflow-tooltip />
      <el-table-column prop="path" label="路由路径" width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="typeMap[row.menuType].type" size="small">{{ typeMap[row.menuType].text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="showDialog(null, row.menuId)">新增</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="600px">
      <el-form :model="menuForm" label-width="80px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="menuForm.parentId"
            :data="tableData"
            :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
            placeholder="选择上级菜单（不选则为顶级）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" required>
          <el-radio-group v-model="menuForm.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单名称" required>
              <el-input v-model="menuForm.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="menuForm.sort" :min="0" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="menuForm.menuType !== 'F'">
          <el-col :span="12">
            <el-form-item label="图标">
              <el-input v-model="menuForm.icon" placeholder="请输入图标名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由路径">
              <el-input v-model="menuForm.path" placeholder="请输入路由路径" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="menuForm.menuType !== 'M'" label="权限标识">
          <el-input v-model="menuForm.perms" placeholder="如：system:user:list" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="menuForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ menuName: '', status: '' })
const isExpand = ref(true)

const typeMap = {
  M: { text: '目录', type: 'primary' },
  C: { text: '菜单', type: 'success' },
  F: { text: '按钮', type: 'warning' }
}

const tableData = ref([
  {
    menuId: 1, menuName: '工作台', icon: '📊', sort: 1, perms: '', path: '/dashboard', menuType: 'C', status: 1, children: []
  },
  {
    menuId: 2, menuName: '审批管理', icon: '📋', sort: 2, perms: '', path: '/approval', menuType: 'M', status: 1,
    children: [
      { menuId: 21, menuName: '待我审批', icon: '', sort: 1, perms: 'approval:pending:list', path: '/approval/pending', menuType: 'C', status: 1, children: [] },
      { menuId: 22, menuName: '已办事项', icon: '', sort: 2, perms: 'approval:done:list', path: '/approval/done', menuType: 'C', status: 1, children: [] },
      { menuId: 23, menuName: '流程监控', icon: '', sort: 3, perms: 'approval:monitor:list', path: '/approval/monitor', menuType: 'C', status: 1, children: [] }
    ]
  },
  {
    menuId: 3, menuName: '事项管理', icon: '📝', sort: 3, perms: '', path: '/item', menuType: 'M', status: 1,
    children: [
      { menuId: 31, menuName: '事项列表', icon: '', sort: 1, perms: 'item:list', path: '/item/list', menuType: 'C', status: 1, children: [] },
      { menuId: 32, menuName: '事项分类', icon: '', sort: 2, perms: 'item:category', path: '/item/category', menuType: 'C', status: 1, children: [] }
    ]
  },
  {
    menuId: 4, menuName: '系统管理', icon: '⚙️', sort: 4, perms: '', path: '/system', menuType: 'M', status: 1,
    children: [
      { menuId: 41, menuName: '用户管理', icon: '', sort: 1, perms: 'system:user:list', path: '/system/user', menuType: 'C', status: 1, children: [
        { menuId: 411, menuName: '用户新增', icon: '', sort: 1, perms: 'system:user:add', path: '', menuType: 'F', status: 1, children: [] },
        { menuId: 412, menuName: '用户编辑', icon: '', sort: 2, perms: 'system:user:edit', path: '', menuType: 'F', status: 1, children: [] },
        { menuId: 413, menuName: '用户删除', icon: '', sort: 3, perms: 'system:user:delete', path: '', menuType: 'F', status: 1, children: [] }
      ]},
      { menuId: 42, menuName: '角色管理', icon: '', sort: 2, perms: 'system:role:list', path: '/system/role', menuType: 'C', status: 1, children: [] },
      { menuId: 43, menuName: '部门管理', icon: '', sort: 3, perms: 'system:dept:list', path: '/system/dept', menuType: 'C', status: 1, children: [] },
      { menuId: 44, menuName: '菜单管理', icon: '', sort: 4, perms: 'system:menu:list', path: '/system/menu', menuType: 'C', status: 1, children: [] }
    ]
  }
])

function toggleExpand() { isExpand.value = !isExpand.value }

const dialogVisible = ref(false)
const isEdit = ref(false)
const menuForm = reactive({ parentId: null, menuType: 'C', menuName: '', icon: '', sort: 0, perms: '', path: '', status: 1 })

function showDialog(row, parentId) {
  isEdit.value = !!row
  if (row) {
    Object.assign(menuForm, { parentId: null, menuType: row.menuType, menuName: row.menuName, icon: row.icon, sort: row.sort, perms: row.perms, path: row.path, status: row.status })
  } else {
    Object.assign(menuForm, { parentId: parentId || null, menuType: 'C', menuName: '', icon: '', sort: 0, perms: '', path: '', status: 1 })
  }
  dialogVisible.value = true
}

function handleSave() {
  if (!menuForm.menuName) return ElMessage.warning('请输入菜单名称')
  ElMessage.success(isEdit.value ? '菜单更新成功' : '菜单创建成功')
  dialogVisible.value = false
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除菜单"${row.menuName}"吗？`, '警告', { type: 'error' }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleStatusChange(row) { ElMessage.success(`菜单已${row.status ? '启用' : '停用'}`) }
function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { menuName: '', status: '' }) }
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
</style>
