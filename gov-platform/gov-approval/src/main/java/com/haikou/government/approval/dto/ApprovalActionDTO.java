package com.haikou.government.approval.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 审批操作 DTO
 *
 * 受理、通过、驳回、退回等操作的通用参数
 *
 * @author xhl
 */
@Data
public class ApprovalActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 申请单ID */
    @NotNull(message = "申请单ID不能为空")
    private Long applicationId;

    /** 审批意见（驳回/退回时必填） */
    private String opinion;

    /** 乐观锁版本号（防并发，前端从详情接口拿到后带过来） */
    private Integer version;
}
