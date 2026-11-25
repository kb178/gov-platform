package com.haikou.government.system.dto;

import com.haikou.government.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询参数
 *
 * @author xhl
 * @since 2026-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends PageQuery {

    /** 角色名称 */
    private String roleName;

    /** 状态（0正常 1停用） */
    private Byte status;
}
