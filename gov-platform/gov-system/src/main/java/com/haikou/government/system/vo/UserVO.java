package com.haikou.government.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息（管理端）
 *
 * @author xhl
 * @since 2026-08-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 真实姓名 */
    private String realName;

    /** 手机号（脱敏） */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别（0未知 1男 2女） */
    private Byte sex;

    /** 头像地址 */
    private String avatar;

    /** 用户类型（1老百姓 2工作人员） */
    private Byte userType;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 状态（0正常 1停用） */
    private Byte status;

    /** 实名认证状态（0未认证 1已认证） */
    private Byte realNameStatus;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 角色ID列表 */
    private List<Long> roleIds;

    /** 角色名称列表 */
    private List<String> roleNames;
}
