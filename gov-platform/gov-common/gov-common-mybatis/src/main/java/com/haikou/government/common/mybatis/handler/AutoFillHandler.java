package com.haikou.government.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
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
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("自动填充 - 插入操作");

        LocalDateTime now = LocalDateTime.now();

        // 使用 strictInsertFill 需要字段有 @TableField(fill = FieldFill.INSERT) 注解
        // 这里改用直接判断字段是否存在并设置值的方式
        if (metaObject.hasSetter("createTime")) {
            this.setFieldValByName("createTime", now, metaObject);
        }
        if (metaObject.hasSetter("updateTime")) {
            this.setFieldValByName("updateTime", now, metaObject);
        }
        if (metaObject.hasSetter("delFlag")) {
            this.setFieldValByName("delFlag", 0, metaObject);
        }

        // TODO: 从 SecurityContext 获取当前用户，填充 createBy 和 updateBy
        // String username = SecurityUtils.getCurrentUsername();
        // if (username != null) {
        //     this.setFieldValByName("createBy", username, metaObject);
        //     this.setFieldValByName("updateBy", username, metaObject);
        // }
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("自动填充 - 更新操作");

        if (metaObject.hasSetter("updateTime")) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }

        // TODO: 从 SecurityContext 获取当前用户，填充 updateBy
        // String username = SecurityUtils.getCurrentUsername();
        // if (username != null) {
        //     this.setFieldValByName("updateBy", username, metaObject);
        // }
    }
}
