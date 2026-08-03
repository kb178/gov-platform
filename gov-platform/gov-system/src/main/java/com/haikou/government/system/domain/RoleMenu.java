package com.haikou.government.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haikou.government.common.core.domain.BaseEntity;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 角色菜单关联表（联合主键）
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
@Getter
@Setter
@TableName("role_menu")
public class RoleMenu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 菜单ID
     */
    @TableField("menu_id")
    private Long menuId;
}
