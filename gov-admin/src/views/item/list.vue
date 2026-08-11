<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">事项列表</h1>
      <span class="desc">管理政务服务事项</span>
    </div>

    <div class="search-bar">
      <el-select v-model="searchForm.category" placeholder="事项分类" clearable style="width: 160px">
        <el-option label="户籍办理" value="household" />
        <el-option label="社会保障" value="social" />
        <el-option label="工商登记" value="business" />
        <el-option label="不动产" value="property" />
        <el-option label="公积金" value="fund" />
      </el-select>
      <el-input v-model="searchForm.name" placeholder="事项名称" clearable style="width: 200px" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="已启用" value="active" />
        <el-option label="已停用" value="inactive" />
        <el-option label="草稿" value="draft" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="ElMessage.info('新增事项')">➕ 新增事项</el-button>
      <el-button @click="ElMessage.info('导出')">📥 导出</el-button>
      <span class="total">共 156 个事项</span>
    </div>

    <el-table :data="tableData">
      <el-table-column label="事项信息" min-width="240">
        <template #default="{ row }">
          <div class="item-name">{{ row.name }}</div>
          <div class="item-code">事项编码：{{ row.code }}</div>
        </template>
      </el-table-column>
      <el-table-column label="所属分类" width="120">
        <template #default="{ row }">
          <el-tag type="primary" size="small" effect="plain">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="dept" label="主管部门" width="120" />
      <el-table-column label="本月办件" width="180">
        <template #default="{ row }">
          <div class="count-cell">
            <span class="count-value">{{ row.count.toLocaleString() }}</span>
            <el-progress :percentage="row.percent" :show-text="false" :stroke-width="8" />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status].type" size="small" effect="dark">
            {{ statusMap[row.status].text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="120" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="ElMessage.info('编辑')">编辑</el-button>
          <el-button type="primary" link size="small" @click="ElMessage.info('指南')">指南</el-button>
          <el-button type="primary" link size="small" @click="ElMessage.info('表单')">表单</el-button>
          <el-button :type="row.status === 'active' ? 'danger' : 'success'" link size="small" @click="toggleStatus(row)">
            {{ row.status === 'active' ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 156 条记录，每页 10 条</span>
      <el-pagination layout="prev, pager, next, jumper" :total="156" :page-size="10" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ category: '', name: '', status: '' })

const statusMap = {
  active: { text: '● 已启用', type: 'success' },
  inactive: { text: '● 已停用', type: 'info' },
  draft: { text: '● 草稿', type: 'warning' }
}

const tableData = ref([
  { name: '居民身份证办理', code: 'HK-HJ-001', category: '户籍办理', dept: '公安局', count: 2356, percent: 90, status: 'active', updateTime: '2024-01-15' },
  { name: '营业执照办理', code: 'HK-GS-001', category: '工商登记', dept: '市场监管局', count: 1892, percent: 75, status: 'active', updateTime: '2024-01-14' },
  { name: '不动产登记', code: 'HK-BD-001', category: '不动产', dept: '自然资源局', count: 1245, percent: 50, status: 'active', updateTime: '2024-01-13' },
  { name: '食品经营许可证', code: 'HK-GS-002', category: '工商登记', dept: '市场监管局', count: 856, percent: 35, status: 'active', updateTime: '2024-01-12' },
  { name: '社保卡申领', code: 'HK-SH-001', category: '社会保障', dept: '人社局', count: 678, percent: 28, status: 'draft', updateTime: '2024-01-11' },
  { name: '公积金提取', code: 'HK-GJ-001', category: '公积金', dept: '公积金中心', count: 543, percent: 22, status: 'inactive', updateTime: '2024-01-10' }
])

function toggleStatus(row) {
  const action = row.status === 'active' ? '停用' : '启用'
  ElMessageBox.confirm(`确定要${action}该事项吗？`, '提示', { type: 'warning' }).then(() => {
    row.status = row.status === 'active' ? 'inactive' : 'active'
    ElMessage.success(`事项已${action}`)
  }).catch(() => {})
}

function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { category: '', name: '', status: '' }) }
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex; align-items: center; gap: 12px; margin-bottom: 16px;
  .total { margin-left: auto; font-size: 13px; color: #909399; }
}
.item-name { font-weight: 500; font-size: 14px; }
.item-code { font-size: 12px; color: #909399; margin-top: 4px; }
.count-cell {
  display: flex; align-items: center; gap: 10px;
  .count-value { font-weight: 600; font-size: 14px; min-width: 50px; }
  .el-progress { flex: 1; }
}
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
</style>
