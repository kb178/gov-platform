-- =============================================
-- 初始化最高权限管理员账户
-- 用户名: admin
-- 密码: admin123
-- =============================================

-- 1. 插入管理员用户
-- 用户名: admin
-- 密码: admin123 (BCrypt加密，Java兼容的$2a$格式)
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `phone`, `user_type`, `status`, `remark`)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', '13800000000', 2, 0, '最高权限管理员')
ON DUPLICATE KEY UPDATE
  `password` = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
  `username` = 'admin',
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
-- 登录用户名: admin
-- 登录密码: admin123
