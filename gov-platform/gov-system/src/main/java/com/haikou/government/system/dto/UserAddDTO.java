package com.haikou.government.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增用户参数（管理员操作）
 *
 * @author xhl
 * @since 2026-08-05
 */
@Data
public class UserAddDTO {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 64, message = "用户名长度4-64个字符")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度6-32个字符")
    private String password;

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

    /** 备注 */
    private String remark;

    /** 角色ID列表 */
    private List<Long> roleIds;
}
