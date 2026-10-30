package com.haikou.government.system.annotation;

import com.haikou.government.system.enums.BusinessType;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * 作用：标注在 Controller 方法上，AOP 会自动记录操作日志
 *
 * @author xhl
 * @since 2026-08-04
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 模块标题（如：用户管理、角色管理）
     */
    String title() default "";

    /**
     * 操作类型（如：新增、修改、删除）
     */
    BusinessType businessType() default BusinessType.OTHER;
}
