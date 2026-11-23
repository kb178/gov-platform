package com.haikou.government.approval.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.approval.dto.ApprovalActionDTO;
import com.haikou.government.approval.service.ApprovalService;
import com.haikou.government.approval.vo.ApplicationVO;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.common.security.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 审批操作 Controller（工作人员端）
 *
 * @author xhl
 */
@Slf4j
@Tag(name = "审批管理", description = "受理、审核、审批、驳回、退回")
@RestController
@RequestMapping("/approval")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    // ==================== 待办 / 已办 ====================

    @Operation(summary = "待我审批", description = "根据角色自动过滤：窗口看待受理、科室看审核中、领导看审批中")
    @GetMapping("/todo")
    public R<IPage<ApplicationVO>> getTodoList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = SecurityUtils.getCurrentUserId();
        Page<ApplicationVO> page = new Page<>(pageNum, pageSize);
        return R.ok(approvalService.getTodoList(page, userId));
    }

    @Operation(summary = "已办列表", description = "我处理过的申请")
    @GetMapping("/done")
    public R<IPage<ApplicationVO>> getDoneList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = SecurityUtils.getCurrentUserId();
        Page<ApplicationVO> page = new Page<>(pageNum, pageSize);
        return R.ok(approvalService.getDoneList(page, userId));
    }

    // ==================== 窗口人员 ====================

    @Operation(summary = "受理", description = "窗口人员受理申请（待受理→审核中）")
    @PostMapping("/accept")
    public R<Void> accept(@Valid @RequestBody ApprovalActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userName = SecurityUtils.getCurrentUsername();
        approvalService.accept(dto, userId, userName);
        return R.ok(null, "受理成功");
    }

    @Operation(summary = "不予受理", description = "窗口人员不予受理（待受理→已驳回，必须填原因）")
    @PostMapping("/rejectAtAccept")
    public R<Void> rejectAtAccept(@Valid @RequestBody ApprovalActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userName = SecurityUtils.getCurrentUsername();
        approvalService.rejectAtAccept(dto, userId, userName);
        return R.ok(null, "已不予受理");
    }

    // ==================== 科室审核 ====================

    @Operation(summary = "审核通过", description = "科室审核通过（审核中→审批中）")
    @PostMapping("/reviewPass")
    public R<Void> reviewPass(@Valid @RequestBody ApprovalActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userName = SecurityUtils.getCurrentUsername();
        approvalService.reviewPass(dto, userId, userName);
        return R.ok(null, "审核通过");
    }

    // ==================== 领导审批 ====================

    @Operation(summary = "审批通过", description = "领导审批通过（审批中→已通过，触发办结）")
    @PostMapping("/approve")
    public R<Void> approve(@Valid @RequestBody ApprovalActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userName = SecurityUtils.getCurrentUsername();
        approvalService.approve(dto, userId, userName);
        return R.ok(null, "审批通过");
    }

    // ==================== 通用操作 ====================

    @Operation(summary = "驳回", description = "科室/领导驳回（审核中/审批中→已驳回，必须填原因）")
    @PostMapping("/reject")
    public R<Void> reject(@Valid @RequestBody ApprovalActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userName = SecurityUtils.getCurrentUsername();
        approvalService.reject(dto, userId, userName);
        return R.ok(null, "已驳回");
    }

    @Operation(summary = "退回补材料", description = "窗口/科室退回（待受理/审核中→退回补材料，必须填原因）")
    @PostMapping("/return")
    public R<Void> returnForMaterial(@Valid @RequestBody ApprovalActionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        String userName = SecurityUtils.getCurrentUsername();
        approvalService.returnForMaterial(dto, userId, userName);
        return R.ok(null, "已退回");
    }
}
