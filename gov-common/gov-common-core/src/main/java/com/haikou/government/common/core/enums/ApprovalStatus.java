package com.haikou.government.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批状态枚举
 *
 * @author gov-platform
 */
@Getter
@AllArgsConstructor
public enum ApprovalStatus {

    /** 草稿 */
    DRAFT(0, "草稿"),

    /** 待受理 */
    PENDING(1, "待受理"),

    /** 已受理 */
    ACCEPTED(2, "已受理"),

    /** 审核中 */
    REVIEWING(3, "审核中"),

    /** 审批通过 */
    APPROVED(4, "审批通过"),

    /** 审批驳回 */
    REJECTED(5, "审批驳回"),

    /** 已办结 */
    COMPLETED(6, "已办结"),

    /** 已撤回 */
    WITHDRAWN(7, "已撤回"),

    /** 已作废 */
    VOIDED(9, "已作废");

    private final int code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static ApprovalStatus getByCode(int code) {
        for (ApprovalStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否可以撤回
     */
    public boolean canWithdraw() {
        return this == PENDING || this == ACCEPTED || this == REVIEWING;
    }

    /**
     * 是否已完结
     */
    public boolean isFinished() {
        return this == COMPLETED || this == VOIDED;
    }
}
