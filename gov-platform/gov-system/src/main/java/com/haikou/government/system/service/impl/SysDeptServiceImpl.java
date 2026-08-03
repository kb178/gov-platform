package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.system.domain.SysDept;
import com.haikou.government.system.dto.DeptDTO;
import com.haikou.government.system.mapper.SysDeptMapper;
import com.haikou.government.system.service.SysDeptService;
import com.haikou.government.system.vo.DeptVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门表 服务实现类
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    /**
     * 查询部门列表（树形结构）
     */
    @Override
    public List<DeptVO> getDeptTree() {
        // 查询所有部门（按排序号升序）
        List<SysDept> depts = this.list(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSortNum));

        // 转换为VO列表
        List<DeptVO> voList = depts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(voList);
    }

    /**
     * 查询部门详情
     */
    @Override
    public DeptVO getDeptById(Long deptId) {
        SysDept dept = this.getById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return convertToVO(dept);
    }

    /**
     * 新增部门
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addDept(DeptDTO deptDTO) {
        // 校验部门名称唯一性（同级下）
        checkDeptNameUnique(deptDTO.getParentId(), deptDTO.getDeptName(), null);

        // 创建部门
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(deptDTO, dept);

        // 设置祖级列表
        if (deptDTO.getParentId() != null && deptDTO.getParentId() != 0) {
            SysDept parent = this.getById(deptDTO.getParentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
            // 如果父部门的ancestors为空，说明父部门是顶级部门
            String parentAncestors = parent.getAncestors();
            if (parentAncestors == null || parentAncestors.isEmpty()) {
                parentAncestors = "0";
            }
            dept.setAncestors(parentAncestors + "," + parent.getDeptId());
        } else {
            dept.setAncestors("0");
            dept.setParentId(0L);
        }

        this.save(dept);
        log.info("新增部门成功: deptId={}, deptName={}", dept.getDeptId(), dept.getDeptName());
        return true;
    }

    /**
     * 修改部门
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDept(DeptDTO deptDTO) {
        Long deptId = deptDTO.getDeptId();
        if (deptId == null) {
            throw new BusinessException("部门ID不能为空");
        }

        // 检查部门是否存在
        SysDept existingDept = this.getById(deptId);
        if (existingDept == null) {
            throw new BusinessException("部门不存在");
        }

        // 不能将自己设为自己的下级
        if (deptDTO.getParentId() != null && deptDTO.getParentId().equals(deptId)) {
            throw new BusinessException("上级部门不能是自己");
        }

        // 校验部门名称唯一性（同级下，排除自身）
        checkDeptNameUnique(deptDTO.getParentId(), deptDTO.getDeptName(), deptId);

        // 更新部门
        SysDept dept = new SysDept();
        BeanUtils.copyProperties(deptDTO, dept);

        // 更新祖级列表
        if (deptDTO.getParentId() != null && deptDTO.getParentId() != 0) {
            SysDept parent = this.getById(deptDTO.getParentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
            // 如果父部门的ancestors为空，说明父部门是顶级部门
            String parentAncestors = parent.getAncestors();
            if (parentAncestors == null || parentAncestors.isEmpty()) {
                parentAncestors = "0";
            }
            dept.setAncestors(parentAncestors + "," + parent.getDeptId());
        } else {
            dept.setAncestors("0");
            dept.setParentId(0L);
        }

        this.updateById(dept);
        log.info("修改部门成功: deptId={}", deptId);
        return true;
    }

    /**
     * 删除部门
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDept(Long deptId) {
        SysDept dept = this.getById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查是否有子部门
        long childCount = this.count(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId));
        if (childCount > 0) {
            throw new BusinessException("该部门下有子部门，不能删除");
        }

        // 删除部门（逻辑删除）
        this.removeById(deptId);
        log.info("删除部门成功: deptId={}", deptId);
        return true;
    }

    /**
     * 校验部门名称唯一性（同级下）
     */
    private void checkDeptNameUnique(Long parentId, String deptName, Long deptId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, parentId != null ? parentId : 0)
                .eq(SysDept::getDeptName, deptName);
        if (deptId != null) {
            wrapper.ne(SysDept::getDeptId, deptId);
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("同级下已存在相同名称的部门");
        }
    }

    /**
     * 实体转VO
     */
    private DeptVO convertToVO(SysDept dept) {
        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(dept, vo);
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    /**
     * 构建树形结构
     */
    private List<DeptVO> buildTree(List<DeptVO> voList) {
        List<DeptVO> tree = new ArrayList<>();
        for (DeptVO vo : voList) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                // 根节点
                tree.add(vo);
            }
        }
        // 为每个根节点设置子节点
        for (DeptVO root : tree) {
            buildChildren(root, voList);
        }
        return tree;
    }

    /**
     * 递归构建子树
     */
    private void buildChildren(DeptVO parent, List<DeptVO> allDepts) {
        for (DeptVO dept : allDepts) {
            if (parent.getDeptId().equals(dept.getParentId())) {
                parent.getChildren().add(dept);
                buildChildren(dept, allDepts);
            }
        }
    }
}
