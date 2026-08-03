package com.haikou.government.system.service;

import com.haikou.government.system.domain.SysRole;
import com.haikou.government.system.dto.RoleDTO;
import com.haikou.government.system.vo.RoleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色表 服务接口
 *
 * @author xhl
 * @since 2026-08-01
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 查询角色列表
     *
     * @return 角色列表
     */
    List<RoleVO> getRoleList();

    /**
     * 查询角色详情
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    RoleVO getRoleById(Long roleId);

    /**
     * 新增角色
     *
     * @param roleDTO 角色参数
     * @return 是否成功
     */
    boolean addRole(RoleDTO roleDTO);

    /**
     * 修改角色
     *
     * @param roleDTO 角色参数
     * @return 是否成功
     */
    boolean updateRole(RoleDTO roleDTO);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long roleId);

    /**
     * 分配菜单权限
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);
}
