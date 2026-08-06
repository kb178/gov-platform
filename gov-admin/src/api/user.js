import request from './index'

// 管理员登录
export function login(data) {
  return request({
    url: '/admin/login',
    method: 'post',
    data
  })
}

// 管理员登出
export function logout() {
  return request({
    url: '/admin/logout',
    method: 'post'
  })
}

// 获取管理员信息
export function getUserInfo() {
  return request({
    url: '/admin/info',
    method: 'get'
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/admin/password',
    method: 'put',
    data
  })
}
