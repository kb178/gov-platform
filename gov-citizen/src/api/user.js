import request from './index'

// 用户登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 发送短信验证码
export function sendSmsCode(phone) {
  return request({
    url: '/user/sms/send',
    method: 'post',
    data: { phone }
  })
}

// 短信验证码登录
export function loginBySms(data) {
  return request({
    url: '/user/login/sms',
    method: 'post',
    data
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}
