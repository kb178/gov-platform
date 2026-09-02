-- =============================================
-- 插入两个测试用户
-- =============================================

-- 1. 管理员用户（用户名: admin，密码: admin123）
INSERT INTO sys_user (user_id, username, password, nickname, phone, user_type, status)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', '13800000000', 2, 0)
ON DUPLICATE KEY UPDATE
  username = 'admin',
  password = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
  status = 0;

-- 2. 普通用户（手机号: 13800000001，密码: user123）
INSERT INTO sys_user (user_id, username, password, nickname, phone, user_type, status)
VALUES (2, '13800000001', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '测试用户', '13800000001', 1, 0)
ON DUPLICATE KEY UPDATE
  phone = '13800000001',
  password = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
  status = 0;

-- 3. 确保有超级管理员角色
INSERT INTO sys_role (role_id, role_name, role_key, sort_num)
VALUES (1, '超级管理员', 'admin', 1)
ON DUPLICATE KEY UPDATE role_name = '超级管理员';

-- 4. 关联管理员用户和角色
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 完成！
-- 管理员登录：用户名 admin，密码 admin123
-- 普通用户登录：手机号 13800000001，密码 user123
