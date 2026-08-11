<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">公告管理</h1>
      <span class="desc">发布和管理系统公告</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.title" placeholder="公告标题" clearable style="width: 200px" />
      <el-select v-model="searchForm.type" placeholder="公告类型" clearable style="width: 140px">
        <el-option label="重要通知" value="important" />
        <el-option label="普通通知" value="normal" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="已发布" value="published" />
        <el-option label="草稿" value="draft" />
        <el-option label="已归档" value="archived" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 发布公告</el-button>
      <span class="total">共 28 条公告</span>
    </div>

    <el-table :data="tableData">
      <el-table-column label="公告信息" min-width="300">
        <template #default="{ row }">
          <div class="notice-title">{{ row.title }}</div>
          <div class="notice-summary">{{ row.summary }}</div>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'important' ? 'danger' : 'info'" size="small" effect="plain">
            {{ row.type === 'important' ? '重要通知' : '普通通知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status].type" size="small" effect="dark">
            {{ statusMap[row.status].text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="160" />
      <el-table-column prop="views" label="阅读量" width="80" />
      <el-table-column prop="publisher" label="发布人" width="80" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="ElMessage.info('查看')">查看</el-button>
          <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
          <el-button v-if="row.status === 'published'" type="warning" link size="small" @click="archiveNotice(row)">归档</el-button>
          <el-button v-if="row.status === 'draft'" type="success" link size="small" @click="publishNotice(row)">发布</el-button>
          <el-button v-if="row.status === 'draft'" type="danger" link size="small" @click="deleteNotice(row)">删除</el-button>
          <el-button v-if="row.status === 'archived'" type="primary" link size="small" @click="republishNotice(row)">重新发布</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 28 条记录，每页 10 条</span>
      <el-pagination layout="prev, pager, next" :total="28" :page-size="10" />
    </div>

    <!-- 发布/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="发布公告" width="700px">
      <el-form :model="noticeForm" label-width="80px">
        <el-form-item label="公告标题" required>
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公告类型" required>
              <el-select v-model="noticeForm.type" style="width: 100%">
                <el-option label="普通通知" value="normal" />
                <el-option label="重要通知" value="important" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="置顶">
              <el-select v-model="noticeForm.top" style="width: 100%">
                <el-option label="否" :value="0" />
                <el-option label="是" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="公告摘要">
          <el-input v-model="noticeForm.summary" type="textarea" :rows="2" placeholder="请输入公告摘要，用于列表展示" />
        </el-form-item>
        <el-form-item label="公告内容" required>
          <el-input v-model="noticeForm.content" type="textarea" :rows="8" placeholder="请输入公告内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDraft">存为草稿</el-button>
        <el-button type="primary" @click="publishFromDialog">立即发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ title: '', type: '', status: '' })

const statusMap = {
  published: { text: '● 已发布', type: 'success' },
  draft: { text: '● 草稿', type: 'warning' },
  archived: { text: '● 已归档', type: 'info' }
}

const tableData = ref([
  { title: '系统升级通知：1月20日凌晨维护', summary: '为提升系统性能和安全性，计划于1月20日凌晨2:00-6:00进行系统维护升级...', type: 'important', status: 'published', publishTime: '2024-01-16 10:00', views: 1256, publisher: '管理员' },
  { title: '关于2024年春节假期值班安排', summary: '根据国务院办公厅通知精神，现将2024年春节假期期间政务服务大厅值班安排通知如下...', type: 'normal', status: 'published', publishTime: '2024-01-15 14:30', views: 892, publisher: '管理员' },
  { title: '新事项办理流程上线通知', summary: '为进一步优化政务服务流程，提升办事效率，新增"公积金提取"等5个事项的网上办理功能...', type: 'normal', status: 'published', publishTime: '2024-01-14 09:00', views: 756, publisher: '管理员' },
  { title: '1月份办件质量分析报告', summary: '2024年1月份办件质量分析报告已生成，请各部门负责人查阅...', type: 'normal', status: 'draft', publishTime: '-', views: '-', publisher: '管理员' },
  { title: '工作人员培训通知', summary: '为提高工作人员业务能力，定于1月25日组织业务培训...', type: 'normal', status: 'published', publishTime: '2024-01-12 16:00', views: 523, publisher: '管理员' },
  { title: '关于优化不动产登记流程的通知', summary: '为进一步方便群众办理不动产登记业务，现对相关流程进行优化调整...', type: 'important', status: 'archived', publishTime: '2024-01-10 11:00', views: 2156, publisher: '管理员' }
])

const dialogVisible = ref(false)
const noticeForm = reactive({ title: '', type: 'normal', top: 0, summary: '', content: '' })

function showDialog(row) {
  if (row) {
    Object.assign(noticeForm, { title: row.title, type: row.type, top: 0, summary: row.summary, content: '' })
  } else {
    Object.assign(noticeForm, { title: '', type: 'normal', top: 0, summary: '', content: '' })
  }
  dialogVisible.value = true
}

function saveDraft() { dialogVisible.value = false; ElMessage.success('已保存为草稿') }
function publishFromDialog() {
  if (!noticeForm.title) return ElMessage.warning('请输入公告标题')
  dialogVisible.value = false; ElMessage.success('公告发布成功')
}
function publishNotice(row) { ElMessage.success('公告已发布') }
function archiveNotice(row) { ElMessageBox.confirm('确定要归档该公告吗？', '提示', { type: 'warning' }).then(() => ElMessage.success('公告已归档')).catch(() => {}) }
function deleteNotice(row) { ElMessageBox.confirm('确定要删除该公告吗？删除后不可恢复。', '警告', { type: 'error' }).then(() => ElMessage.success('公告已删除')).catch(() => {}) }
function republishNotice(row) { ElMessageBox.confirm('确定要重新发布该公告吗？', '提示', { type: 'warning' }).then(() => ElMessage.success('公告已重新发布')).catch(() => {}) }
function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { title: '', type: '', status: '' }) }
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
.notice-title { font-weight: 500; font-size: 14px; color: #1F2937; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 350px; }
.notice-summary { font-size: 12px; color: #909399; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 350px; }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
</style>
