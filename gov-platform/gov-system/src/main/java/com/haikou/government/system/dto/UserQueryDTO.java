package com.haikou.government.system.dto;

import com.haikou.government.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询参数
 *
 * @author xhl
 * @since 2026-08-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQuery {

    /** 用户名 */
    private String username;

    /** 手机号 */
    private String phone;

    /** 用户类型（1老百姓 2工作人员） */
    private Byte userType;

    /** 状态（0正常 1停用） */
    private Byte status;

    /** 部门ID */
    private Long deptId;
}
