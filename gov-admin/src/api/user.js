import request from './index'

// 管理员登录（用户名+密码）
export function adminLogin(data) {
  return request({
    url: '/system/sysUser/adminLogin',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export function getUserInfo() {
  return request({
    url: '/system/sysUser/info',
    method: 'get'
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/system/sysUser/changePassword',
    method: 'post',
    data
  })
}

// 找回密码
export function resetPassword(data) {
  return request({
    url: '/system/sysUser/resetPassword',
    method: 'post',
    data
  })
}

// 手机号注册
export function register(data) {
  return request({
    url: '/system/sysUser/register',
    method: 'post',
    data
  })
}

// 实名认证
export function realNameAuth(data) {
  return request({
    url: '/system/sysUser/realNameAuth',
    method: 'post',
    data
  })
}

// 查询实名认证状态
export function getRealNameStatus() {
  return request({
    url: '/system/sysUser/realNameStatus',
    method: 'get'
  })
}

// 修改个人信息
export function updateUserInfo(data) {
  return request({
    url: '/system/sysUser/info',
    method: 'put',
    data
  })
}
