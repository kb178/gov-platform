package com.haikou.government.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 *
 * @author gov-platform
 */
@Getter
@AllArgsConstructor
public enum UserType {

    /** 老百姓 */
    CITIZEN(1, "老百姓"),

    /** 工作人员 */
    STAFF(2, "工作人员"),

    /** 管理员 */
    ADMIN(3, "管理员");

    private final int code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static UserType getByCode(int code) {
        for (UserType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
