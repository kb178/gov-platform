package com.haikou.government.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.haikou.government.common.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 *
 * @author gov-platform
 */
@Slf4j
@Component
public class AutoFillHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     *
     * 填充字段：createTime、updateTime、delFlag、createBy、updateBy
     * 只有实体中存在的字段才会填充（hasSetter 判断）
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("自动填充 - 插入操作");

        LocalDateTime now = LocalDateTime.now();

        // 时间和删除标志
        if (metaObject.hasSetter("createTime")) {
            this.setFieldValByName("createTime", now, metaObject);
        }
        if (metaObject.hasSetter("updateTime")) {
            this.setFieldValByName("updateTime", now, metaObject);
        }
        if (metaObject.hasSetter("delFlag")) {
            this.setFieldValByName("delFlag", 0, metaObject);
        }

        // 创建人和更新人（从 Security 上下文获取当前登录用户名）
        String username = SecurityUtils.getCurrentUsername();
        if (username != null) {
            if (metaObject.hasSetter("createBy")) {
                this.setFieldValByName("createBy", username, metaObject);
            }
            if (metaObject.hasSetter("updateBy")) {
                this.setFieldValByName("updateBy", username, metaObject);
            }
        }
    }

    /**
     * 更新时自动填充
     *
     * 填充字段：updateTime、updateBy
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("自动填充 - 更新操作");

        if (metaObject.hasSetter("updateTime")) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }

        // 更新人（从 Security 上下文获取当前登录用户名）
        String username = SecurityUtils.getCurrentUsername();
        if (username != null && metaObject.hasSetter("updateBy")) {
            this.setFieldValByName("updateBy", username, metaObject);
        }
    }
}
