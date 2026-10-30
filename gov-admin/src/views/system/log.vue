<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">操作日志</h1>
      <span class="desc">查看系统操作记录</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.operName" placeholder="操作人" clearable style="width: 140px" />
      <el-select v-model="searchForm.businessType" placeholder="操作类型" clearable style="width: 140px">
        <el-option label="新增" :value="1" />
        <el-option label="修改" :value="2" />
        <el-option label="删除" :value="3" />
        <el-option label="查询" :value="4" />
        <el-option label="授权" :value="5" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="成功" :value="0" />
        <el-option label="失败" :value="1" />
      </el-select>
      <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" value-format="YYYY-MM-DD" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="danger" @click="handleClear">🗑️ 清空日志</el-button>
      <span class="total">共 {{ total }} 条记录</span>
    </div>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="operId" label="日志编号" width="80" />
      <el-table-column prop="title" label="操作模块" width="140" />
      <el-table-column label="操作类型" width="90">
        <template #default="{ row }">
          <el-tag :type="typeTagMap[row.businessType]" size="small">{{ typeTextMap[row.businessType] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operName" label="操作人" width="100" />
      <el-table-column prop="operIp" label="操作IP" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operTime" label="操作时间" width="170" />
      <el-table-column label="耗时" width="80">
        <template #default="{ row }}">{{ row.costTime }}ms</template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ total }} 条记录，每页 {{ pageSize }} 条</span>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        layout="prev, pager, next, jumper"
        :total="total"
        @current-change="loadData"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="操作详情" width="600px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="操作模块">{{ currentLog.title }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ typeTextMap[currentLog.businessType] }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.operIp }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.status === 0 ? 'success' : 'danger'" size="small">
            {{ currentLog.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.operTime }}</el-descriptions-item>
        <el-descriptions-item label="请求方式" :span="2">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ currentLog.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="log-params">{{ currentLog.operParam }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2" v-if="currentLog.jsonResult">
          <div class="log-params">{{ currentLog.jsonResult }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOperLogPage, getOperLogDetail, cleanOperLog } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  operName: '',
  businessType: '',
  status: '',
  dateRange: []
})

const typeTagMap = {
  1: 'success',
  2: 'warning',
  3: 'danger',
  4: 'info',
  5: 'primary'
}

const typeTextMap = {
  1: '新增',
  2: '修改',
  3: '删除',
  4: '查询',
  5: '授权',
  6: '导出',
  7: '导入',
  8: '强退',
  9: '清空'
}

const detailVisible = ref(false)
const currentLog = ref(null)

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      operName: searchForm.operName || undefined,
      businessType: searchForm.businessType || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined,
      beginTime: searchForm.dateRange?.[0] || undefined,
      endTime: searchForm.dateRange?.[1] || undefined
    }
    const res = await getOperLogPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载操作日志失败', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function resetSearch() {
  Object.assign(searchForm, { operName: '', businessType: '', status: '', dateRange: [] })
  handleSearch()
}

async function showDetail(row) {
  try {
    const res = await getOperLogDetail(row.operId)
    currentLog.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error('获取日志详情失败', error)
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复。', '警告', { type: 'error' })
    await cleanOperLog()
    ElMessage.success('日志已清空')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空日志失败', error)
    }
  }
}
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
.log-params { max-height: 100px; overflow-y: auto; word-break: break-all; font-size: 13px; color: #606266; background: #F9FAFB; padding: 8px; border-radius: 4px; }
</style>
