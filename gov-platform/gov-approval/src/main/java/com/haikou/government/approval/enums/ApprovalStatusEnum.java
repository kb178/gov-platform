package com.haikou.government.approval.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 申请单状态枚举 + 状态流转规则
 *
 * 状态机设计：不允许乱跳状态，只能走合法的流转路径
 *
 * @author xhl
 */
public enum ApprovalStatusEnum {

    DRAFT(0, "草稿"),
    PENDING(1, "待受理"),
    REVIEWING(2, "审核中"),
    RETURNED(3, "退回补材料"),
    APPROVING(4, "审批中"),
    REJECTED(5, "已驳回"),
    APPROVED(6, "已通过");

    private final int code;
    private final String desc;

    ApprovalStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ApprovalStatusEnum fromCode(Byte code) {
        for (ApprovalStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 获取状态描述
     */
    public static String getDesc(Byte code) {
        ApprovalStatusEnum e = fromCode(code);
        return e != null ? e.desc : "未知";
    }

    /**
     * 校验状态流转是否合法
     *
     * @param from 当前状态
     * @param to   目标状态
     * @return true=合法
     */
    public static boolean canTransit(Byte from, Byte to) {
        ApprovalStatusEnum fromEnum = fromCode(from);
        ApprovalStatusEnum toEnum = fromCode(to);
        if (fromEnum == null || toEnum == null) {
            return false;
        }
        return getAllowedTransitions(fromEnum).contains(toEnum);
    }

    /**
     * 获取某个状态允许流转到的目标状态
     *
     * 合法流转路径：
     *   草稿(0)     → 待受理(1)          用户提交
     *   待受理(1)   → 审核中(2)          窗口受理
     *   待受理(1)   → 退回(3)            窗口退回补材料
     *   待受理(1)   → 已驳回(5)          不予受理
     *   待受理(1)   → 草稿(0)            用户撤回
     *   审核中(2)   → 审批中(4)          科室审核通过
     *   审核中(2)   → 已驳回(5)          科室驳回
     *   审核中(2)   → 退回(3)            科室退回补材料
     *   审批中(4)   → 已通过(6)          领导审批通过
     *   审批中(4)   → 已驳回(5)          领导驳回
     *   退回(3)     → 待受理(1)          用户补材料重新提交
     */
    private static List<ApprovalStatusEnum> getAllowedTransitions(ApprovalStatusEnum from) {
        switch (from) {
            case DRAFT:
                return List.of(PENDING);
            case PENDING:
                return List.of(REVIEWING, RETURNED, REJECTED, DRAFT);
            case REVIEWING:
                return List.of(APPROVING, REJECTED, RETURNED);
            case RETURNED:
                return List.of(PENDING);
            case APPROVING:
                return List.of(APPROVED, REJECTED);
            case REJECTED:
            case APPROVED:
                return List.of(); // 终态，不能流转
            default:
                return List.of();
        }
    }

    /**
     * 判断是否是终态（已通过/已驳回）
     */
    public static boolean isFinal(Byte code) {
        return code != null && (code == APPROVED.getCode() || code == REJECTED.getCode());
    }
}
