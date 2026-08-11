<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">待我审批</h1>
      <span class="desc">处理待审批的事项申请</span>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <el-tag
        v-for="tab in filterTabs"
        :key="tab.key"
        :effect="activeFilter === tab.key ? 'dark' : 'plain'"
        class="filter-tab"
        @click="activeFilter = tab.key"
      >
        {{ tab.label }}
        <span class="count">{{ tab.count }}</span>
      </el-tag>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select v-model="searchForm.type" placeholder="事项类型" clearable style="width: 160px">
        <el-option label="户籍办理" value="household" />
        <el-option label="工商登记" value="business" />
        <el-option label="社会保障" value="social" />
        <el-option label="不动产" value="property" />
      </el-select>
      <el-input v-model="searchForm.applicant" placeholder="申请人姓名" clearable style="width: 180px" />
      <el-date-picker v-model="searchForm.date" type="date" placeholder="申请时间" style="width: 180px" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 批量操作 -->
    <transition name="el-fade-in">
      <div v-if="selectedRows.length" class="batch-bar">
        <span>已选择 <strong>{{ selectedRows.length }}</strong> 项</span>
        <el-button type="success" size="small">批量通过</el-button>
        <el-button type="danger" size="small">批量驳回</el-button>
        <el-button size="small" @click="clearSelection">取消选择</el-button>
      </div>
    </transition>

    <!-- 表格 -->
    <el-table :data="tableData" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column label="申请人" min-width="160">
        <template #default="{ row }">
          <div class="applicant-cell">
            <div class="applicant-avatar">{{ row.applicant.charAt(0) }}</div>
            <div>
              <div class="applicant-name">{{ row.applicant }}</div>
              <div class="applicant-id">身份证：{{ row.idCard }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="事项名称" min-width="180">
        <template #default="{ row }">
          <div class="item-name">{{ row.itemName }}</div>
          <div class="item-category">{{ row.category }}</div>
        </template>
      </el-table-column>
      <el-table-column label="紧急程度" width="100">
        <template #default="{ row }">
          <el-tag :type="row.urgent ? 'danger' : 'success'" size="small" effect="plain">
            {{ row.urgent ? '🚨 加急' : '✓ 普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="160" />
      <el-table-column label="剩余时间" width="120">
        <template #default="{ row }">
          <span :class="['remain-time', row.remainClass]">{{ row.remainText }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showApproval(row)">审批</el-button>
          <el-button type="primary" link size="small">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <span class="pagination-info">共 48 条记录，每页 10 条</span>
      <el-pagination layout="prev, pager, next" :total="48" :page-size="10" />
    </div>

    <!-- 审批弹窗 -->
    <el-dialog v-model="dialogVisible" title="审批处理" width="600px">
      <div class="approval-info" v-if="currentRow">
        <div class="info-row"><span class="label">事项名称：</span><span>{{ currentRow.itemName }}</span></div>
        <div class="info-row"><span class="label">申请人：</span><span>{{ currentRow.applicant }}</span></div>
        <div class="info-row"><span class="label">申请时间：</span><span>{{ currentRow.applyTime }}</span></div>
      </div>

      <el-form label-width="80px">
        <el-form-item label="审批结果" required>
          <div class="result-options">
            <div :class="['result-option', 'approve', { selected: approvalResult === 'approve' }]" @click="approvalResult = 'approve'">
              <div class="result-icon">✅</div>
              <div>通过</div>
            </div>
            <div :class="['result-option', 'reject', { selected: approvalResult === 'reject' }]" @click="approvalResult = 'reject'">
              <div class="result-icon">❌</div>
              <div>驳回</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="审批意见" required>
          <el-input v-model="approvalComment" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
        <el-form-item v-if="approvalResult === 'reject'" label="驳回原因" required>
          <el-select v-model="rejectReason" placeholder="请选择驳回原因" style="width: 100%">
            <el-option label="材料不齐全" value="material" />
            <el-option label="信息填写错误" value="info" />
            <el-option label="不符合申请条件" value="qualify" />
            <el-option label="其他原因" value="other" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApproval">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeFilter = ref('all')
const filterTabs = [
  { key: 'all', label: '全部', count: 48 },
  { key: 'urgent', label: '加急', count: 5 },
  { key: 'today', label: '今日新增', count: 12 },
  { key: 'timeout', label: '即将超时', count: 3 }
]

const searchForm = reactive({ type: '', applicant: '', date: '' })

const tableData = ref([
  { applicant: '张三', idCard: '460102****1234', itemName: '营业执照申请', category: '工商登记', urgent: true, applyTime: '2024-01-16 09:30', remainText: '剩余 2 小时', remainClass: 'danger' },
  { applicant: '李四', idCard: '460102****5678', itemName: '居民身份证换领', category: '户籍办理', urgent: false, applyTime: '2024-01-16 10:15', remainText: '剩余 3 天', remainClass: '' },
  { applicant: '王五', idCard: '460102****9012', itemName: '食品经营许可证申请', category: '工商登记', urgent: false, applyTime: '2024-01-16 11:00', remainText: '剩余 5 天', remainClass: '' },
  { applicant: '赵六', idCard: '460102****3456', itemName: '不动产登记', category: '不动产', urgent: true, applyTime: '2024-01-16 14:20', remainText: '剩余 1 天', remainClass: 'warning' },
  { applicant: '钱七', idCard: '460102****7890', itemName: '公积金提取申请', category: '公积金', urgent: false, applyTime: '2024-01-16 15:45', remainText: '剩余 5 天', remainClass: '' }
])

const selectedRows = ref([])
const handleSelectionChange = (rows) => { selectedRows.value = rows }
const clearSelection = () => { selectedRows.value = [] }

const dialogVisible = ref(false)
const currentRow = ref(null)
const approvalResult = ref('')
const approvalComment = ref('')
const rejectReason = ref('')

function showApproval(row) {
  currentRow.value = row
  approvalResult.value = ''
  approvalComment.value = ''
  rejectReason.value = ''
  dialogVisible.value = true
}

function submitApproval() {
  if (!approvalResult.value) return ElMessage.warning('请选择审批结果')
  if (!approvalComment.value) return ElMessage.warning('请输入审批意见')
  ElMessage.success('审批提交成功')
  dialogVisible.value = false
}

function handleSearch() { ElMessage.info('搜索功能待对接接口') }
</script>

<style lang="scss" scoped>
.filter-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .filter-tab {
    cursor: pointer;
    padding: 8px 16px;
    border-radius: 20px;

    .count {
      margin-left: 6px;
      opacity: 0.8;
    }
  }
}

.batch-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 20px;
  background: #EFF6FF;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #1E40AF;
}

.applicant-cell { display: flex; align-items: center; gap: 10px; }
.applicant-avatar {
  width: 32px; height: 32px; border-radius: 50%; background: #DBEAFE;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; color: #1E40AF; font-weight: 600; flex-shrink: 0;
}
.applicant-name { font-weight: 500; font-size: 14px; }
.applicant-id { font-size: 12px; color: #909399; margin-top: 2px; }
.item-name { font-weight: 500; font-size: 14px; }
.item-category { font-size: 12px; color: #909399; margin-top: 2px; }
.remain-time { font-size: 13px; &.warning { color: #E6A23C; } &.danger { color: #F56C6C; } }

.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }

.approval-info {
  background: #F9FAFB; border-radius: 8px; padding: 16px; margin-bottom: 20px;
  .info-row { margin-bottom: 8px; font-size: 14px; &:last-child { margin-bottom: 0; } .label { color: #909399; margin-right: 8px; } }
}

.result-options {
  display: flex; gap: 16px; width: 100%;
  .result-option {
    flex: 1; padding: 20px; border: 2px solid #E5E7EB; border-radius: 8px;
    text-align: center; cursor: pointer; transition: all 0.2s; font-size: 14px; font-weight: 500;
    .result-icon { font-size: 32px; margin-bottom: 8px; }
    &.approve.selected { border-color: #67C23A; background: #F0FDF4; }
    &.reject.selected { border-color: #F56C6C; background: #FEF2F2; }
    &:hover { border-color: #409EFF; }
  }
}
</style>
