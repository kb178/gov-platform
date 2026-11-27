import request from './index'

// 保存草稿（存到数据库）
export function saveDraft(data) {
  return request({
    url: '/approval/application/draft',
    method: 'post',
    data
  })
}

// 修改草稿
export function updateDraft(applicationId, data) {
  return request({
    url: `/approval/application/draft/${applicationId}`,
    method: 'put',
    data
  })
}

// 删除草稿
export function deleteDraft(applicationId) {
  return request({
    url: `/approval/application/draft/${applicationId}`,
    method: 'delete'
  })
}

// 提交申请
export function submitApplication(data) {
  return request({
    url: '/approval/application/submit',
    method: 'post',
    data
  })
}

// 我的申请列表
export function getMyApplications(params) {
  return request({
    url: '/approval/application/my',
    method: 'get',
    params
  })
}

// 申请详情
export function getApplicationDetail(applicationId) {
  return request({
    url: `/approval/application/${applicationId}`,
    method: 'get'
  })
}

// 审批记录
export function getApprovalRecords(applicationId) {
  return request({
    url: `/approval/application/record/${applicationId}`,
    method: 'get'
  })
}

// 上传文件到 MinIO
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/approval/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
