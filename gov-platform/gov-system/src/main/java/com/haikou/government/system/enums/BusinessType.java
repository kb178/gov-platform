package com.haikou.government.system.enums;

/**
 * 业务操作类型枚举
 *
 * code 与前端操作日志页的类型编号一致：
 * 0其他 1新增 2修改 3删除 4查询 5授权 6导出
 *
 * @author xhl
 * @since 2026-08-04
 */
public enum BusinessType {

    /** 其他 */
    OTHER(0, "其他"),

    /** 新增 */
    INSERT(1, "新增"),

    /** 修改 */
    UPDATE(2, "修改"),

    /** 删除 */
    DELETE(3, "删除"),

    /** 查询 */
    QUERY(4, "查询"),

    /** 授权 */
    GRANT(5, "授权"),

    /** 导出 */
    EXPORT(6, "导出");

    private final int code;

    private final String desc;

    BusinessType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
