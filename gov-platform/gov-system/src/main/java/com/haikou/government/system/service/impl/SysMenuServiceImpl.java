package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.system.domain.SysMenu;
import com.haikou.government.system.dto.MenuDTO;
import com.haikou.government.system.mapper.SysMenuMapper;
import com.haikou.government.system.service.SysMenuService;
import com.haikou.government.system.vo.MenuVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    /**
     * 查询菜单列表（树形结构）
     *
     * @return 菜单树形列表
     */
    @Override
    public List<MenuVO> getMenuTree() {
        // 1. 查询所有菜单
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(SysMenu::getSortNum);
        List<SysMenu> menuList = list(queryWrapper);

        // 2. 转换为 VO
        List<MenuVO> voList = menuList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 3. 构建树形结构
        return buildTree(voList, 0L);
    }

    /**
     * 查询菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @Override
    public MenuVO getMenuById(Long menuId) {
        SysMenu menu = getById(menuId);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return convertToVO(menu);
    }

    /**
     * 新增菜单
     *
     * @param menuDTO 菜单参数
     * @return 是否成功
     */
    @Override
    public boolean addMenu(MenuDTO menuDTO) {
        // 1. 校验菜单名称是否重复
        checkMenuNameUnique(menuDTO.getMenuName(), menuDTO.getParentId(), null);

        // 2. 转换为实体
        SysMenu menu = new SysMenu();
        menu.setMenuName(menuDTO.getMenuName());
        menu.setParentId(menuDTO.getParentId() != null ? menuDTO.getParentId() : 0L);
        menu.setSortNum(menuDTO.getSortNum() != null ? menuDTO.getSortNum() : 0);
        menu.setPath(menuDTO.getPath());
        menu.setComponent(menuDTO.getComponent());
        menu.setMenuType(menuDTO.getMenuType());
        menu.setPerms(menuDTO.getPerms());
        menu.setIcon(menuDTO.getIcon());
        menu.setVisible(menuDTO.getVisible() != null ? menuDTO.getVisible() : (byte) 0);
        menu.setStatus(menuDTO.getStatus() != null ? menuDTO.getStatus() : (byte) 0);

        // 3. 保存到数据库
        boolean success = save(menu);
        if (success) {
            log.info("新增菜单成功，menuId={}, menuName={}", menu.getMenuId(), menu.getMenuName());
        }
        return success;
    }

    /**
     * 修改菜单
     *
     * @param menuDTO 菜单参数
     * @return 是否成功
     */
    @Override
    public boolean updateMenu(MenuDTO menuDTO) {
        // 1. 校验菜单是否存在
        SysMenu menu = getById(menuDTO.getMenuId());
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 2. 校验菜单名称是否重复
        checkMenuNameUnique(menuDTO.getMenuName(), menuDTO.getParentId(), menuDTO.getMenuId());

        // 3. 更新字段
        menu.setMenuName(menuDTO.getMenuName());
        menu.setParentId(menuDTO.getParentId() != null ? menuDTO.getParentId() : menu.getParentId());
        menu.setSortNum(menuDTO.getSortNum() != null ? menuDTO.getSortNum() : menu.getSortNum());
        menu.setPath(menuDTO.getPath());
        menu.setComponent(menuDTO.getComponent());
        menu.setMenuType(menuDTO.getMenuType());
        menu.setPerms(menuDTO.getPerms());
        menu.setIcon(menuDTO.getIcon());
        menu.setVisible(menuDTO.getVisible() != null ? menuDTO.getVisible() : menu.getVisible());
        menu.setStatus(menuDTO.getStatus() != null ? menuDTO.getStatus() : menu.getStatus());

        // 4. 保存到数据库
        boolean success = updateById(menu);
        if (success) {
            log.info("修改菜单成功，menuId={}, menuName={}", menu.getMenuId(), menu.getMenuName());
        }
        return success;
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 是否成功
     */
    @Override
    public boolean deleteMenu(Long menuId) {
        // 1. 校验菜单是否存在
        SysMenu menu = getById(menuId);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 2. 校验是否有子菜单
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getParentId, menuId);
        Long childCount = baseMapper.selectCount(queryWrapper);
        if (childCount > 0) {
            throw new BusinessException("该菜单下有子菜单，不能删除");
        }

        // 3. 删除菜单
        boolean success = removeById(menuId);
        if (success) {
            log.info("删除菜单成功，menuId={}, menuName={}", menuId, menu.getMenuName());
        }
        return success;
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menuName 菜单名称
     * @param parentId 父菜单ID
     * @param menuId 菜单ID（修改时传入）
     */
    private void checkMenuNameUnique(String menuName, Long parentId, Long menuId) {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getMenuName, menuName)
                    .eq(SysMenu::getParentId, parentId != null ? parentId : 0L);
        if (menuId != null) {
            queryWrapper.ne(SysMenu::getMenuId, menuId);
        }
        Long count = baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("同级菜单下已存在该菜单名称");
        }
    }

    /**
     * 将实体转换为 VO
     *
     * @param menu 菜单实体
     * @return 菜单VO
     */
    private MenuVO convertToVO(SysMenu menu) {
        return MenuVO.builder()
                .menuId(menu.getMenuId())
                .menuName(menu.getMenuName())
                .parentId(menu.getParentId())
                .sortNum(menu.getSortNum())
                .path(menu.getPath())
                .component(menu.getComponent())
                .menuType(menu.getMenuType())
                .perms(menu.getPerms())
                .icon(menu.getIcon())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .children(new ArrayList<>())
                .build();
    }

    /**
     * 构建树形结构
     *
     * @param voList 所有菜单VO列表
     * @param parentId 父菜单ID
     * @return 树形菜单列表
     */
    private List<MenuVO> buildTree(List<MenuVO> voList, Long parentId) {
        return voList.stream()
                .filter(vo -> parentId.equals(vo.getParentId()))
                .peek(vo -> vo.setChildren(buildTree(voList, vo.getMenuId())))
                .collect(Collectors.toList());
    }
}
