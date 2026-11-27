<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">待我审批</h1>
      <span class="desc">处理待审批的事项申请</span>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="申请人/办件编号" clearable style="width: 200px" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column label="办件编号" width="180">
        <template #default="{ row }">
          <span class="app-no">{{ row.applicationNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请人" width="100">
        <template #default="{ row }">
          <div class="applicant-cell">
            <div class="applicant-avatar">{{ (row.userName || '?').charAt(0) }}</div>
            <span>{{ row.userName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="itemName" label="事项名称" min-width="180" />
      <el-table-column label="紧急程度" width="90">
        <template #default="{ row }">
          <el-tag :type="row.urgency === 1 ? 'danger' : 'success'" size="small" effect="plain">
            {{ row.urgency === 1 ? '加急' : '普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="statusText" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showApproval(row)">审批</el-button>
          <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ total }} 条记录</span>
      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 审批弹窗 -->
    <el-dialog v-model="dialogVisible" title="审批处理" width="600px">
      <div class="approval-info" v-if="currentRow">
        <div class="info-row"><span class="label">办件编号：</span><span>{{ currentRow.applicationNo }}</span></div>
        <div class="info-row"><span class="label">事项名称：</span><span>{{ currentRow.itemName }}</span></div>
        <div class="info-row"><span class="label">申请人：</span><span>{{ currentRow.userName }}</span></div>
        <div class="info-row"><span class="label">申请时间：</span><span>{{ currentRow.applyTime }}</span></div>
        <div class="info-row"><span class="label">当前状态：</span><el-tag size="small">{{ currentRow.statusText }}</el-tag></div>
      </div>

      <el-form label-width="90px">
        <el-form-item label="审批操作" required>
          <div class="result-options">
            <!-- 根据当前状态显示可选操作 -->
            <template v-if="currentRow?.status === 1">
              <div :class="['result-option', 'accept', { selected: approvalAction === 'accept' }]" @click="approvalAction = 'accept'">
                <div class="result-icon">✅</div><div>受理</div>
              </div>
              <div :class="['result-option', 'return', { selected: approvalAction === 'return' }]" @click="approvalAction = 'return'">
                <div class="result-icon">↩️</div><div>退回补材料</div>
              </div>
              <div :class="['result-option', 'reject', { selected: approvalAction === 'rejectAtAccept' }]" @click="approvalAction = 'rejectAtAccept'">
                <div class="result-icon">❌</div><div>不予受理</div>
              </div>
            </template>
            <template v-else-if="currentRow?.status === 2">
              <div :class="['result-option', 'accept', { selected: approvalAction === 'reviewPass' }]" @click="approvalAction = 'reviewPass'">
                <div class="result-icon">✅</div><div>审核通过</div>
              </div>
              <div :class="['result-option', 'return', { selected: approvalAction === 'return' }]" @click="approvalAction = 'return'">
                <div class="result-icon">↩️</div><div>退回补材料</div>
              </div>
              <div :class="['result-option', 'reject', { selected: approvalAction === 'reject' }]" @click="approvalAction = 'reject'">
                <div class="result-icon">❌</div><div>驳回</div>
              </div>
            </template>
            <template v-else-if="currentRow?.status === 4">
              <div :class="['result-option', 'accept', { selected: approvalAction === 'approve' }]" @click="approvalAction = 'approve'">
                <div class="result-icon">✅</div><div>审批通过</div>
              </div>
              <div :class="['result-option', 'reject', { selected: approvalAction === 'reject' }]" @click="approvalAction = 'reject'">
                <div class="result-icon">❌</div><div>驳回</div>
              </div>
            </template>
          </div>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approvalOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitApproval">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="700px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="办件编号">{{ detailData.applicationNo }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="事项名称">{{ detailData.itemName }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag size="small">{{ detailData.statusText }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detailData.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="加急">{{ detailData.urgency === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 表单数据 -->
      <div v-if="detailFormData" class="detail-section">
        <h4>申请信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item v-for="(val, key) in detailFormData" :key="key" :label="key">{{ val }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 材料清单 -->
      <div v-if="detailMaterials?.length" class="detail-section">
        <h4>申请材料</h4>
        <el-table :data="detailMaterials" size="small">
          <el-table-column prop="materialName" label="材料名称" />
          <el-table-column label="是否必须" width="80">
            <template #default="{ row }">{{ row.required === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="核验状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.checkStatus === 1 ? 'success' : row.checkStatus === 2 ? 'danger' : 'info'">
                {{ row.checkStatus === 1 ? '已核验' : row.checkStatus === 2 ? '未通过' : '待核验' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="附件" width="80">
            <template #default="{ row }">
              <el-button v-if="row.fileUrl" type="primary" link size="small" @click="previewFile(row.fileUrl)">查看</el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 审批记录 -->
      <div v-if="detailRecords?.length" class="detail-section">
        <h4>审批记录</h4>
        <el-timeline>
          <el-timeline-item v-for="rec in detailRecords" :key="rec.id" :timestamp="rec.createTime" placement="top">
            <div><strong>{{ rec.nodeName }}</strong> - {{ rec.approverName }} {{ actionText(rec.action) }}</div>
            <div v-if="rec.opinion" class="record-opinion">{{ rec.opinion }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTodoList, accept, rejectAtAccept, reviewPass, approve, reject, returnForMaterial, getApplicationDetail, getApprovalRecords } from '@/api/approval'

// ==================== 列表 ====================
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({ keyword: '' })

async function loadList() {
  loading.value = true
  try {
    const res = await getTodoList({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data?.records || []
    total.value = Number(res.data?.total) || 0
  } catch (e) {
    console.error('加载待办列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadList()
}

function handleReset() {
  searchForm.keyword = ''
  pageNum.value = 1
  loadList()
}

function handlePageChange(page) {
  pageNum.value = page
  loadList()
}

function statusTagType(status) {
  const map = { 1: 'warning', 2: 'primary', 4: 'primary', 3: 'info', 5: 'danger', 6: 'success' }
  return map[status] || 'info'
}

// ==================== 审批操作 ====================
const dialogVisible = ref(false)
const currentRow = ref(null)
const approvalAction = ref('')
const approvalOpinion = ref('')
const submitting = ref(false)

function showApproval(row) {
  currentRow.value = row
  approvalAction.value = ''
  approvalOpinion.value = ''
  dialogVisible.value = true
}

async function submitApproval() {
  if (!approvalAction.value) return ElMessage.warning('请选择审批操作')

  // 驳回/退回/不予受理 必须填意见
  const needOpinion = ['reject', 'rejectAtAccept', 'return']
  if (needOpinion.includes(approvalAction.value) && !approvalOpinion.value.trim()) {
    return ElMessage.warning('该操作必须填写审批意见')
  }

  submitting.value = true
  try {
    const data = { applicationId: currentRow.value.applicationId, opinion: approvalOpinion.value }
    const actionMap = {
      accept, rejectAtAccept, reviewPass, approve, reject, return: returnForMaterial
    }
    await actionMap[approvalAction.value](data)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    console.error('审批操作失败', e)
  } finally {
    submitting.value = false
  }
}

// ==================== 详情 ====================
const detailVisible = ref(false)
const detailData = ref(null)
const detailFormData = ref(null)
const detailMaterials = ref([])
const detailRecords = ref([])

async function showDetail(row) {
  try {
    const [detailRes, recordRes] = await Promise.all([
      getApplicationDetail(row.applicationId),
      getApprovalRecords(row.applicationId)
    ])
    detailData.value = detailRes.data
    detailMaterials.value = detailRes.data?.materials || []
    detailRecords.value = recordRes.data || []

    // 解析表单数据 JSON
    const formDataStr = detailRes.data?.formData
    if (formDataStr) {
      try {
        const parsed = JSON.parse(formDataStr)
        delete parsed._uploadImages // 去掉内部字段
        detailFormData.value = parsed
      } catch { detailFormData.value = null }
    } else {
      detailFormData.value = null
    }

    detailVisible.value = true
  } catch (e) {
    console.error('获取详情失败', e)
  }
}

function previewFile(url) {
  window.open(url, '_blank')
}

function actionText(action) {
  const map = { 1: '受理', 2: '通过', 3: '驳回', 4: '退回补材料', 5: '不予受理' }
  return map[action] || ''
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.app-no { font-family: monospace; font-size: 13px; }

.applicant-cell { display: flex; align-items: center; gap: 8px; }
.applicant-avatar {
  width: 28px; height: 28px; border-radius: 50%; background: #DBEAFE;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; color: #1E40AF; font-weight: 600; flex-shrink: 0;
}

.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }

.approval-info {
  background: #F9FAFB; border-radius: 8px; padding: 16px; margin-bottom: 20px;
  .info-row { margin-bottom: 8px; font-size: 14px; &:last-child { margin-bottom: 0; } .label { color: #909399; margin-right: 8px; } }
}

.result-options {
  display: flex; gap: 12px; width: 100%; flex-wrap: wrap;
  .result-option {
    flex: 1; min-width: 100px; padding: 16px 12px; border: 2px solid #E5E7EB; border-radius: 8px;
    text-align: center; cursor: pointer; transition: all 0.2s; font-size: 13px; font-weight: 500;
    .result-icon { font-size: 28px; margin-bottom: 6px; }
    &.accept.selected { border-color: #67C23A; background: #F0FDF4; }
    &.reject.selected { border-color: #F56C6C; background: #FEF2F2; }
    &.return.selected { border-color: #E6A23C; background: #FDF6EC; }
    &:hover { border-color: #409EFF; }
  }
}

.detail-section {
  margin-top: 20px;
  h4 { margin-bottom: 12px; font-size: 14px; color: #303133; border-left: 3px solid #409EFF; padding-left: 8px; }
}

.record-opinion { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
