package com.haikou.government.system.controller;

import com.haikou.government.common.core.domain.R;
import com.haikou.government.system.dto.DeptDTO;
import com.haikou.government.system.service.SysDeptService;
import com.haikou.government.system.vo.DeptVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理 前端控制器
 *
 * @author xhl
 * @since 2026-08-01
 */
@Tag(name = "部门管理", description = "部门增删改查接口")
@RestController
@RequestMapping("/sysDept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    /**
     * 查询部门列表（树形结构）
     *
     * @return 部门树形列表
     */
    @Operation(summary = "查询部门列表", description = "获取所有部门，返回树形结构")
    @GetMapping("/list")
    public R<List<DeptVO>> list() {
        List<DeptVO> deptTree = sysDeptService.getDeptTree();
        return R.ok(deptTree);
    }

    /**
     * 查询部门详情
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    @Operation(summary = "查询部门详情", description = "根据部门ID查询部门详细信息")
    @GetMapping("/{deptId}")
    public R<DeptVO> getById(@PathVariable Long deptId) {
        DeptVO deptVO = sysDeptService.getDeptById(deptId);
        return R.ok(deptVO);
    }

    /**
     * 新增部门
     *
     * @param deptDTO 部门参数
     * @return 是否成功
     */
    @Operation(summary = "新增部门", description = "添加新部门")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody DeptDTO deptDTO) {
        boolean result = sysDeptService.addDept(deptDTO);
        return R.ok(result);
    }

    /**
     * 修改部门
     *
     * @param deptDTO 部门参数
     * @return 是否成功
     */
    @Operation(summary = "修改部门", description = "修改部门信息")
    @PutMapping
    public R<Boolean> update(@Valid @RequestBody DeptDTO deptDTO) {
        boolean result = sysDeptService.updateDept(deptDTO);
        return R.ok(result);
    }

    /**
     * 删除部门
     *
     * @param deptId 部门ID
     * @return 是否成功
     */
    @Operation(summary = "删除部门", description = "删除部门（不能删除有子部门的部门）")
    @DeleteMapping("/{deptId}")
    public R<Boolean> delete(@PathVariable Long deptId) {
        boolean result = sysDeptService.deleteDept(deptId);
        return R.ok(result);
    }
}
