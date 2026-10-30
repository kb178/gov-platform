package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.system.domain.RoleMenu;
import com.haikou.government.system.domain.SysRole;
import com.haikou.government.system.dto.RoleDTO;
import com.haikou.government.system.mapper.RoleMenuMapper;
import com.haikou.government.system.mapper.SysRoleMapper;
import com.haikou.government.system.service.SysRoleService;
import com.haikou.government.system.vo.RoleVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色表 服务实现类
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    /**
     * 查询角色列表
     */
    @Override
    public List<RoleVO> getRoleList() {
        // 查询所有角色（按排序号升序）
        List<SysRole> roles = this.list(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getSortNum));

        // 转换为VO列表
        return roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询角色详情
     */
    @Override
    public RoleVO getRoleById(Long roleId) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return convertToVO(role);
    }

    /**
     * 新增角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRole(RoleDTO roleDTO) {
        // 校验角色名称唯一性
        checkRoleNameUnique(roleDTO.getRoleName(), null);

        // 校验角色标识唯一性
        checkRoleKeyUnique(roleDTO.getRoleKey(), null);

        // 创建角色
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        this.save(role);

        // 如果有菜单权限，保存角色-菜单关联
        if (!CollectionUtils.isEmpty(roleDTO.getMenuIds())) {
            saveRoleMenus(role.getRoleId(), roleDTO.getMenuIds());
        }

        log.info("新增角色成功: roleId={}, roleName={}", role.getRoleId(), role.getRoleName());
        return true;
    }

    /**
     * 修改角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleDTO roleDTO) {
        Long roleId = roleDTO.getRoleId();
        if (roleId == null) {
            throw new BusinessException("角色ID不能为空");
        }

        // 检查角色是否存在
        SysRole existingRole = this.getById(roleId);
        if (existingRole == null) {
            throw new BusinessException("角色不存在");
        }

        // 校验角色名称唯一性（排除自身）
        checkRoleNameUnique(roleDTO.getRoleName(), roleId);

        // 校验角色标识唯一性（排除自身）
        checkRoleKeyUnique(roleDTO.getRoleKey(), roleId);

        // 更新角色
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        this.updateById(role);

        // 如果有菜单权限，更新角色-菜单关联
        if (roleDTO.getMenuIds() != null) {
            // 先删除原有关联
            roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>()
                    .eq(RoleMenu::getRoleId, roleId));
            // 保存新的关联
            if (!CollectionUtils.isEmpty(roleDTO.getMenuIds())) {
                saveRoleMenus(roleId, roleDTO.getMenuIds());
            }
        }

        log.info("修改角色成功: roleId={}", roleId);
        return true;
    }

    /**
     * 删除角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除角色
        this.removeById(roleId);

        // 删除角色-菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId));

        log.info("删除角色成功: roleId={}", roleId);
        return true;
    }

    /**
     * 分配菜单权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        // 检查角色是否存在
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 先删除原有关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>()
                .eq(RoleMenu::getRoleId, roleId));

        // 保存新的关联
        if (!CollectionUtils.isEmpty(menuIds)) {
            saveRoleMenus(roleId, menuIds);
        }

        log.info("分配菜单权限成功: roleId={}, menuCount={}", roleId, menuIds == null ? 0 : menuIds.size());
        return true;
    }

    /**
     * 校验角色名称唯一性
     */
    private void checkRoleNameUnique(String roleName, Long roleId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleName, roleName);
        if (roleId != null) {
            wrapper.ne(SysRole::getRoleId, roleId);
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("角色名称已存在");
        }
    }

    /**
     * 校验角色标识唯一性
     */
    private void checkRoleKeyUnique(String roleKey, Long roleId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleKey);
        if (roleId != null) {
            wrapper.ne(SysRole::getRoleId, roleId);
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("角色标识已存在");
        }
    }

    /**
     * 保存角色-菜单关联
     */
    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }

    /**
     * 实体转VO（包含查询菜单ID列表）
     */
    private RoleVO convertToVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);

        // 查询该角色关联的菜单ID列表
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, role.getRoleId()));
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
        vo.setMenuIds(menuIds);

        return vo;
    }
}
