package com.haikou.government.approval.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批记录表
 *
 * 每一步审批操作都记一条，形成完整的审批痕迹（audit trail）
 * 不继承 BaseEntity：记录本身只关心谁在什么时候做了什么，不需要更新审计
 *
 * @author xhl
 * @since 2026-11-23
 */
@Data
@TableName("approval_record")
public class ApprovalRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @TableId(value = "record_id", type = IdType.ASSIGN_ID)
    private Long recordId;

    /** 申请单ID */
    @TableField("application_id")
    private Long applicationId;

    /** 审批节点名称（窗口受理/科室审核/领导审批） */
    @TableField("node_name")
    private String nodeName;

    /** 审批人ID（sys_user） */
    @TableField("approver_id")
    private Long approverId;

    /** 审批人姓名（冗余存储） */
    @TableField("approver_name")
    private String approverName;

    /** 操作（1受理 2通过 3驳回 4退回补材料 5不予受理） */
    @TableField("action")
    private Byte action;

    /** 审批意见 */
    @TableField("opinion")
    private String opinion;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
