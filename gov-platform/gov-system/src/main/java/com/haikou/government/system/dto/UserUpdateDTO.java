package com.haikou.government.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改用户参数（管理员操作）
 *
 * @author xhl
 * @since 2026-08-05
 */
@Data
public class UserUpdateDTO {

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别（0未知 1男 2女） */
    private Byte sex;

    /** 用户类型（1老百姓 2工作人员） */
    private Byte userType;

    /** 部门ID */
    private Long deptId;

    /** 状态（0正常 1停用） */
    private Byte status;

    /** 备注 */
    private String remark;
}
