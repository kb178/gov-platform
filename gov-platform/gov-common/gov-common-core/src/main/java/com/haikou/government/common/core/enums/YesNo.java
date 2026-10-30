package com.haikou.government.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否枚举
 *
 * @author gov-platform
 */
@Getter
@AllArgsConstructor
public enum YesNo {

    /** 否 */
    NO(0, "否"),

    /** 是 */
    YES(1, "是");

    private final int code;
    private final String desc;

    public static YesNo getByCode(int code) {
        for (YesNo yesNo : values()) {
            if (yesNo.code == code) {
                return yesNo;
            }
        }
        return null;
    }

    public boolean isYes() {
        return this == YES;
    }

    public boolean isNo() {
        return this == NO;
    }
}
