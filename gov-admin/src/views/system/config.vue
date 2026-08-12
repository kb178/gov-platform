<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="title">系统参数</h1>
      <span class="desc">管理系统配置参数</span>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.name" placeholder="参数名称" clearable style="width: 180px" />
      <el-input v-model="searchForm.key" placeholder="参数键名" clearable style="width: 180px" />
      <el-select v-model="searchForm.type" placeholder="系统内置" clearable style="width: 140px">
        <el-option label="是" value="Y" />
        <el-option label="否" value="N" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">➕ 新增参数</el-button>
      <el-button @click="ElMessage.info('刷新缓存')">🔄 刷新缓存</el-button>
      <span class="total">共 {{ tableData.length }} 个参数</span>
    </div>

    <el-table :data="tableData">
      <el-table-column prop="id" label="编号" width="70" />
      <el-table-column prop="name" label="参数名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="key" label="参数键名" width="240" show-overflow-tooltip />
      <el-table-column prop="value" label="参数键值" width="180" show-overflow-tooltip />
      <el-table-column label="系统内置" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'Y' ? 'primary' : 'info'" size="small">
            {{ row.type === 'Y' ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="showDialog(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)" :disabled="row.type === 'Y'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <span class="pagination-info">共 {{ tableData.length }} 条记录，每页 10 条</span>
      <el-pagination layout="prev, pager, next, jumper" :total="tableData.length" :page-size="10" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑参数' : '新增参数'" width="500px">
      <el-form :model="configForm" label-width="90px">
        <el-form-item label="参数名称" required>
          <el-input v-model="configForm.name" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数键名" required>
          <el-input v-model="configForm.key" placeholder="请输入参数键名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="参数键值" required>
          <el-input v-model="configForm.value" placeholder="请输入参数键值" />
        </el-form-item>
        <el-form-item label="系统内置">
          <el-radio-group v-model="configForm.type">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="configForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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

const searchForm = reactive({ name: '', key: '', type: '' })

const tableData = ref([
  { id: 1, name: '主框架页-默认皮肤', key: 'sys.index.skinName', value: 'skin-blue', type: 'Y', remark: '蓝色 skin-blue / 绿色 skin-green / 紫色 skin-purple' },
  { id: 2, name: '用户管理-账号初始密码', key: 'sys.user.initPassword', value: '123456', type: 'Y', remark: '初始化密码 123456' },
  { id: 3, name: '主框架页-侧边栏主题', key: 'sys.index.sideTheme', value: 'theme-dark', type: 'Y', remark: '深色主题 theme-dark / 浅色主题 theme-light' },
  { id: 4, name: '账号自助-验证码开关', key: 'sys.account.captchaEnabled', value: 'true', type: 'Y', remark: '是否开启验证码功能（true/false）' },
  { id: 5, name: '账号自助-是否开启注册', key: 'sys.account.registerEnabled', value: 'false', type: 'Y', remark: '是否开启注册功能（true/false）' },
  { id: 6, name: '短信验证码有效期', key: 'sys.sms.code.expire', value: '300', type: 'N', remark: '短信验证码有效期（秒）' },
  { id: 7, name: '登录失败锁定次数', key: 'sys.login.maxRetry', value: '5', type: 'N', remark: '登录失败N次后锁定账号' },
  { id: 8, name: '登录失败锁定时间', key: 'sys.login.lockTime', value: '15', type: 'N', remark: '锁定时间（分钟）' },
  { id: 9, name: '文件上传大小限制', key: 'sys.upload.maxSize', value: '10', type: 'N', remark: '文件上传大小限制（MB）' },
  { id: 10, name: '审批超时提醒', key: 'approval.timeout.remind', value: 'true', type: 'N', remark: '审批即将超时时是否发送提醒（true/false）' }
])

const dialogVisible = ref(false)
const isEdit = ref(false)
const configForm = reactive({ name: '', key: '', value: '', type: 'N', remark: '' })

function showDialog(row) {
  isEdit.value = !!row
  if (row) {
    Object.assign(configForm, { name: row.name, key: row.key, value: row.value, type: row.type, remark: row.remark })
  } else {
    Object.assign(configForm, { name: '', key: '', value: '', type: 'N', remark: '' })
  }
  dialogVisible.value = true
}

function handleSave() {
  if (!configForm.name || !configForm.key || !configForm.value) return ElMessage.warning('请填写完整信息')
  ElMessage.success(isEdit.value ? '参数更新成功' : '参数创建成功')
  dialogVisible.value = false
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除参数"${row.name}"吗？`, '警告', { type: 'error' }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function handleSearch() { ElMessage.info('搜索功能待对接接口') }
function resetSearch() { Object.assign(searchForm, { name: '', key: '', type: '' }) }
</script>

<style lang="scss" scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; .total { margin-left: auto; font-size: 13px; color: #909399; } }
.pagination-wrap { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; }
.pagination-info { font-size: 13px; color: #909399; }
</style>
