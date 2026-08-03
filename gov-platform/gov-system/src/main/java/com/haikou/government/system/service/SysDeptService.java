package com.haikou.government.system.service;

import com.haikou.government.system.domain.SysDept;
import com.haikou.government.system.dto.DeptDTO;
import com.haikou.government.system.vo.DeptVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 部门表 服务接口
 *
 * @author xhl
 * @since 2026-08-01
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 查询部门列表（树形结构）
     *
     * @return 部门树形列表
     */
    List<DeptVO> getDeptTree();

    /**
     * 查询部门详情
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    DeptVO getDeptById(Long deptId);

    /**
     * 新增部门
     *
     * @param deptDTO 部门参数
     * @return 是否成功
     */
    boolean addDept(DeptDTO deptDTO);

    /**
     * 修改部门
     *
     * @param deptDTO 部门参数
     * @return 是否成功
     */
    boolean updateDept(DeptDTO deptDTO);

    /**
     * 删除部门
     *
     * @param deptId 部门ID
     * @return 是否成功
     */
    boolean deleteDept(Long deptId);
}
