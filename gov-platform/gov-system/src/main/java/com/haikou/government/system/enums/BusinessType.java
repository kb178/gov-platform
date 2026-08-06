package com.haikou.government.system.enums;

/**
 * 业务操作类型枚举
 *
 * @author xhl
 * @since 2026-08-04
 */
public enum BusinessType {

    /** 新增 */
    INSERT("新增"),

    /** 修改 */
    UPDATE("修改"),

    /** 删除 */
    DELETE("删除"),

    /** 查询 */
    QUERY("查询"),

    /** 导出 */
    EXPORT("导出"),

    /** 授权 */
    GRANT("授权"),

    /** 其他 */
    OTHER("其他");

    private final String desc;

    BusinessType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
