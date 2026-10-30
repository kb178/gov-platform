-- =============================================
-- 初始化最高权限管理员账户（安全版）
-- =============================================
-- 使用方法：
-- 1. 先执行此SQL创建用户（密码为admin123）
-- 2. 启动应用后，登录并修改密码为admin
-- =============================================

-- 1. 插入管理员用户（密码: admin123，BCrypt加密）
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `user_type`, `status`, `remark`)
VALUES (1, 'admin', '$2a$10$VQECfCqt3FI8MnkzIUCqH.sECWGJDFv5O3MJ3mORlBoVx3iYXBwwS', '超级管理员', 2, 0, '最高权限管理员')
ON DUPLICATE KEY UPDATE
  `password` = '$2a$10$VQECfCqt3FI8MnkzIUCqH.sECWGJDFv5O3MJ3mORlBoVx3iYXBwwS',
  `status` = 0;

-- 2. 确保有超级管理员角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `sort_num`, `remark`)
VALUES (1, '超级管理员', 'admin', 1, '拥有所有权限')
ON DUPLICATE KEY UPDATE
  `role_name` = '超级管理员',
  `status` = 0;

-- 3. 关联用户和角色
INSERT IGNORE INTO `user_role` (`user_id`, `role_id`)
VALUES (1, 1);

-- 4. 确保超级管理员拥有所有菜单权限
INSERT IGNORE INTO `role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu`;

-- 完成！
-- 默认账户: admin
-- 默认密码: admin123
--
-- 登录后请立即修改密码为 admin：
-- 调用接口: POST /sysUser/changePassword
-- 参数: { "oldPassword": "admin123", "newPassword": "admin", "confirmPassword": "admin" }
