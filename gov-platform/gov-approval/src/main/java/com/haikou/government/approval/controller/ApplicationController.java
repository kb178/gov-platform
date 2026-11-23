package com.haikou.government.approval.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.approval.domain.ApprovalRecord;
import com.haikou.government.approval.dto.ApplicationSubmitDTO;
import com.haikou.government.approval.service.ApprovalRecordService;
import com.haikou.government.approval.service.ApprovalService;
import com.haikou.government.approval.service.ApplicationService;
import com.haikou.government.approval.vo.ApplicationDetailVO;
import com.haikou.government.approval.vo.ApplicationVO;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.common.security.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 申请单 Controller
 *
 * @author xhl
 */
@Slf4j
@Tag(name = "申请管理", description = "申请的提交、草稿、查询")
@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApprovalRecordService approvalRecordService;

    @Autowired
    private ApprovalService approvalService;

    // ==================== 提交申请 ====================

    /**
     * 提交申请
     */
    @Operation(summary = "提交申请", description = "创建申请单+表单数据+材料，状态变为待受理")
    @PostMapping("/submit")
    public R<Long> submitApplication(@Valid @RequestBody ApplicationSubmitDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long applicationId = applicationService.submitApplication(dto, userId);
        return R.ok(applicationId, "申请提交成功");
    }

    // ==================== 草稿 ====================

    /**
     * 保存草稿
     */
    @Operation(summary = "保存草稿", description = "保存为草稿状态，可后续修改再提交")
    @PostMapping("/draft")
    public R<Long> saveDraft(@RequestBody ApplicationSubmitDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long applicationId = applicationService.saveDraft(dto, userId);
        return R.ok(applicationId, "草稿保存成功");
    }

    /**
     * 修改草稿
     */
    @Operation(summary = "修改草稿", description = "只能修改草稿状态的申请")
    @PutMapping("/draft/{applicationId}")
    public R<Void> updateDraft(@PathVariable Long applicationId,
                               @RequestBody ApplicationSubmitDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        applicationService.updateDraft(applicationId, dto, userId);
        return R.ok(null, "草稿修改成功");
    }

    /**
     * 删除草稿
     */
    @Operation(summary = "删除草稿", description = "只能删除草稿状态的申请")
    @DeleteMapping("/draft/{applicationId}")
    public R<Void> deleteDraft(@PathVariable Long applicationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        applicationService.deleteDraft(applicationId, userId);
        return R.ok(null, "草稿删除成功");
    }

    // ==================== 撤回 / 重新提交 ====================

    @Operation(summary = "撤回申请", description = "待受理状态可撤回，回到草稿")
    @PostMapping("/withdraw/{applicationId}")
    public R<Void> withdraw(@PathVariable Long applicationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        approvalService.withdraw(applicationId, userId);
        return R.ok(null, "撤回成功");
    }

    @Operation(summary = "重新提交", description = "退回补材料后重新提交")
    @PostMapping("/resubmit/{applicationId}")
    public R<Void> resubmit(@PathVariable Long applicationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        approvalService.resubmit(applicationId, userId);
        return R.ok(null, "重新提交成功");
    }

    // ==================== 查询 ====================

    /**
     * 我的申请列表
     */
    @Operation(summary = "我的申请列表", description = "查看当前用户的申请，支持按状态筛选")
    @GetMapping("/my")
    public R<IPage<ApplicationVO>> getMyApplications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Byte status) {

        Long userId = SecurityUtils.getCurrentUserId();
        Page<ApplicationVO> page = new Page<>(pageNum, pageSize);
        IPage<ApplicationVO> result = applicationService.getApplicationPage(page, userId, status);
        return R.ok(result);
    }

    /**
     * 查询申请详情
     */
    @Operation(summary = "查询申请详情", description = "含表单数据、材料清单、审批记录")
    @GetMapping("/{applicationId}")
    public R<ApplicationDetailVO> getApplicationDetail(@PathVariable Long applicationId) {
        ApplicationDetailVO vo = applicationService.getApplicationDetail(applicationId);
        return R.ok(vo);
    }

    /**
     * 查询审批记录
     */
    @Operation(summary = "查询审批记录", description = "查看某个申请的完整审批记录")
    @GetMapping("/record/{applicationId}")
    public R<List<ApprovalRecord>> getApprovalRecords(@PathVariable Long applicationId) {
        List<ApprovalRecord> records = approvalRecordService.getRecordsByApplicationId(applicationId);
        return R.ok(records);
    }
}
