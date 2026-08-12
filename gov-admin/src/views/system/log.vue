<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">操作日志</h1>
      <span class="desc">查看系统操作记录</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.operator" placeholder="操作人" clearable style="width: 140px" />
      <el-select v-model="searchForm.type" placeholder="操作类型" clearable style="width: 140px">
        <el-option label="新增" value="INSERT" />
        <el-option label="修改" value="UPDATE" />
        <el-option label="删除" value="DELETE" />
        <el-option label="查询" value="QUERY" />
        <el-option label="授权" value="GRANT" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="成功" :value="0" />
        <el-option label="失败" :value="1" />
      </el-select>
      <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="danger" @click="handleClear">🗑️ 清空日志</el-button>
      <el-button @click="ElMessage.info('导出')">📥 导出</el-button>
      <span class="total">共 {{ tableData.length }} 条记录</span>
    </div>

    <el-table :data="tableData">
      <el-table-column prop="logId" label="日志编号" width="80" />
      <el-table-column prop="title" label="操作模块" width="140" />
      <el-table-column label="操作类型" width="90">
        <template #default="{ row }">
          <el-tag :type="typeTagMap[row.businessType]" size="small">{{ row.businessType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="ip" label="操作IP" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operTime" label="操作时间" width="160" />
      <el-table-column label="耗时" width="80">
        <template #default="{ row }">{{ row.costTime }}ms</template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ tableData.length }} 条记录，每页 10 条</span>
      <el-pagination layout="prev, pager, next, jumper" :total="tableData.length" :page-size="10" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="操作详情" width="600px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="操作模块">{{ currentLog.title }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.businessType }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.status === 0 ? 'success' : 'danger'" size="small">
            {{ currentLog.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.operTime }}</el-descriptions-item>
        <el-descriptions-item label="请求方式" :span="2">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ currentLog.url }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="log-params">{{ currentLog.params }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ operator: '', type: '', status: '', dateRange: [] })

const typeTagMap = {
  INSERT: 'success',
  UPDATE: 'warning',
  DELETE: 'danger',
  QUERY: 'info',
  GRANT: 'primary'
}

const tableData = ref([
  { logId: 1, title: '用户管理', businessType: 'INSERT', operator: '管理员', ip: '192.168.1.100', status: 0, operTime: '2024-01-16 16:30:22', costTime: 45, method: 'POST', url: '/sysUser/addUser', params: '{"username":"zhangke","nickname":"张科长","phone":"13912346666"}' },
  { logId: 2, title: '角色管理', businessType: 'GRANT', operator: '管理员', ip: '192.168.1.100', status: 0, operTime: '2024-01-16 15:20:10', costTime: 32, method: 'PUT', url: '/sysRole/assignPerms', params: '{"roleId":4,"menuIds":[21,22,23]}' },
  { logId: 3, title: '事项管理', businessType: 'UPDATE', operator: '管理员', ip: '192.168.1.100', status: 0, operTime: '2024-01-16 14:15:05', costTime: 28, method: 'PUT', url: '/item/update', params: '{"itemId":1,"status":"active"}' },
  { logId: 4, title: '用户登录', businessType: 'QUERY', operator: '张科长', ip: '192.168.1.101', status: 0, operTime: '2024-01-16 10:30:00', costTime: 120, method: 'POST', url: '/sysUser/login', params: '{"username":"zhangke"}' },
  { logId: 5, title: '公告管理', businessType: 'DELETE', operator: '管理员', ip: '192.168.1.100', status: 0, operTime: '2024-01-16 09:10:33', costTime: 18, method: 'DELETE', url: '/notice/delete/4', params: '{}' },
  { logId: 6, title: '用户管理', businessType: 'UPDATE', operator: '管理员', ip: '192.168.1.100', status: 1, operTime: '2024-01-15 17:45:22', costTime: 55, method: 'PUT', url: '/sysUser/resetPwd', params: '{"userId":4}' },
  { logId: 7, title: '部门管理', businessType: 'INSERT', operator: '管理员', ip: '192.168.1.100', status: 0, operTime: '2024-01-15 16:20:10', costTime: 22, method: 'POST', url: '/sysDept/add', params: '{"deptName":"综合窗口","parentId":11}' },
  { logId: 8, title: '字典管理', businessType: 'UPDATE', operator: '管理员', ip: '192.168.1.100', status: 0, operTime: '2024-01-15 15:05:44', costTime: 15, method: 'PUT', url: '/sysDict/update', params: '{"dictId":1,"status":1}' }
])

const detailVisible = ref(false)
const currentLog = ref(null)

function showDetail(row) {
  currentLog.value = row
  detailVisible.value = true
}

function handleClear() {
  ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复。', '警告', { type: 'error' }).then(() => {
    ElMessage.success('日志已清空')
  }).catch(() => {})
}

function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { operator: '', type: '', status: '', dateRange: [] }) }
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
.log-params { max-height: 100px; overflow-y: auto; word-break: break-all; font-size: 13px; color: #606266; background: #F9FAFB; padding: 8px; border-radius: 4px; }
</style>
