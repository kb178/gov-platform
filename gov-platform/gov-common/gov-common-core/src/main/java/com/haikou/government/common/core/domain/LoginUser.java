package com.haikou.government.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户信息
 *
 * @author gov-platform
 */
@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 用户类型（1老百姓 2工作人员） */
    private Integer userType;

    /** 部门ID */
    private Long deptId;

    /** 角色集合 */
    private Set<String> roles;

    /** 权限集合 */
    private Set<String> perms;

    /** Token */
    private String token;
}
