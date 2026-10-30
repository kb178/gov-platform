
# 用户表添加一个实名认证时间
ALTER TABLE gov_system.sys_user
    ADD COLUMN verify_time DATETIME COMMENT '实名认证时间' AFTER real_name_status;