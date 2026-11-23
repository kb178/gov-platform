package com.haikou.government.approval.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.approval.domain.ApprovalRecord;
import com.haikou.government.approval.service.ApprovalRecordService;
import com.haikou.government.approval.service.ApplicationService;
import com.haikou.government.approval.vo.ApplicationVO;
import com.haikou.government.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 申请单 Controller
 *
 * 第一阶段：基础 CRUD 接口
 *
 * @author xhl
 */
@Slf4j
@Tag(name = "申请管理", description = "申请单的增删改查")
@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApprovalRecordService approvalRecordService;

    /**
     * 分页查询申请列表
     */
    @Operation(summary = "分页查询申请列表", description = "支持按申请人、状态筛选")
    @GetMapping("/list")
    public R<IPage<ApplicationVO>> getApplicationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Byte status) {

        Page<ApplicationVO> page = new Page<>(pageNum, pageSize);
        IPage<ApplicationVO> result = applicationService.getApplicationPage(page, userId, status);
        return R.ok(result);
    }

    /**
     * 查询申请详情
     */
    @Operation(summary = "查询申请详情", description = "根据申请单ID查询详情")
    @GetMapping("/{applicationId}")
    public R<ApplicationVO> getApplicationDetail(@PathVariable Long applicationId) {
        ApplicationVO vo = applicationService.getApplicationDetail(applicationId);
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
