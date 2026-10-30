package com.haikou.government.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author gov-platform
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 通用错误 1xxx ====================
    SUCCESS(200, "操作成功"),
    FAIL(1000, "操作失败"),
    PARAM_ERROR(1001, "参数错误"),
    PARAM_NULL(1002, "参数不能为空"),
    DATA_NOT_FOUND(1003, "数据不存在"),
    DATA_ALREADY_EXISTS(1004, "数据已存在"),
    OPERATION_NOT_ALLOWED(1005, "操作不允许"),

    // ==================== 用户相关 2xxx ====================
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_PASSWORD_ERROR(2002, "密码错误"),
    USER_DISABLED(2003, "用户已禁用"),
    USER_LOCKED(2004, "用户已锁定"),
    USER_ALREADY_EXISTS(2005, "用户已存在"),
    CAPTCHA_ERROR(2006, "验证码错误"),
    CAPTCHA_EXPIRED(2007, "验证码已过期"),
    TOKEN_EXPIRED(2008, "Token已过期"),
    TOKEN_INVALID(2009, "Token无效"),
    LOGIN_REQUIRED(2010, "请先登录"),

    // ==================== 权限相关 3xxx ====================
    PERMISSION_DENIED(3001, "权限不足"),
    ROLE_NOT_FOUND(3002, "角色不存在"),
    MENU_NOT_FOUND(3003, "菜单不存在"),

    // ==================== 审批相关 4xxx ====================
    APPROVAL_NOT_FOUND(4001, "审批单不存在"),
    APPROVAL_STATUS_ERROR(4002, "审批状态不允许此操作"),
    APPROVAL_ALREADY_PROCESSED(4003, "该审批单已处理"),
    FLOW_NOT_FOUND(4004, "流程不存在"),
    FLOW_DEPLOY_ERROR(4005, "流程部署失败"),

    // ==================== 事项相关 5xxx ====================
    ITEM_NOT_FOUND(5001, "事项不存在"),
    ITEM_NOT_PUBLISHED(5002, "事项未发布"),
    ITEM_CATEGORY_NOT_FOUND(5003, "事项分类不存在"),

    // ==================== 证照相关 6xxx ====================
    LICENSE_NOT_FOUND(6001, "证照不存在"),
    LICENSE_EXPIRED(6002, "证照已过期"),
    LICENSE_TEMPLATE_NOT_FOUND(6003, "证照模板不存在"),
    LICENSE_GENERATE_ERROR(6004, "证照生成失败"),

    // ==================== 文件相关 7xxx ====================
    FILE_UPLOAD_ERROR(7001, "文件上传失败"),
    FILE_NOT_FOUND(7002, "文件不存在"),
    FILE_TYPE_NOT_ALLOWED(7003, "文件类型不允许"),
    FILE_SIZE_EXCEEDED(7004, "文件大小超出限制");

    private final int code;
    private final String desc;

    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return null;
    }
}
