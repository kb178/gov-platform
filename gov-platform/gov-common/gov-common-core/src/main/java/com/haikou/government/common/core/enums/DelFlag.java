package com.haikou.government.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 删除标志枚举
 *
 * @author gov-platform
 */
@Getter
@AllArgsConstructor
public enum DelFlag {

    /** 正常 */
    NORMAL(0, "正常"),

    /** 已删除 */
    DELETED(1, "已删除");

    private final int code;
    private final String desc;
}
