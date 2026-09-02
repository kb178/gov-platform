-- =============================================
-- 初始化管理员账户
-- 手机号: 13800000000
-- 密码: admin123
-- =============================================

-- 删除旧数据（按顺序）
DELETE FROM `user_role` WHERE `user_id` = 1;
DELETE FROM `role_menu` WHERE `role_id` = 1;

-- 插入管理员用户（密码: admin123 的 BCrypt 加密）
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `nickname`, `phone`, `user_type`, `status`, `remark`)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', '13800000000', 2, 0, '最高权限管理员');

-- 确保有超级管理员角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `sort_num`, `remark`)
VALUES (1, '超级管理员', 'admin', 1, '拥有所有权限');

-- 关联用户和角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 确保超级管理员拥有所有菜单权限
INSERT INTO `role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu`;

-- 完成！
-- 登录手机号: 13800000000
-- 登录密码: admin123
