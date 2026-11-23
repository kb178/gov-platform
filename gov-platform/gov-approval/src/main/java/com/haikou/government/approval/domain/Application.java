package com.haikou.government.approval.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.haikou.government.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 申请单表
 *
 * 一次办事申请 = 一条记录，是审批模块的核心表
 *
 * @author xhl
 * @since 2026-11-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("application")
public class Application extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 申请单ID */
    @TableId(value = "application_id", type = IdType.ASSIGN_ID)
    private Long applicationId;

    /** 申请编号（业务编号，如：SQ202611230001） */
    @TableField("application_no")
    private String applicationNo;

    /** 申请人ID（sys_user） */
    @TableField("user_id")
    private Long userId;

    /** 办理事项ID（item_info） */
    @TableField("item_id")
    private Long itemId;

    /** 状态（0草稿 1待受理 2审核中 3退回补材料 4审批中 5已驳回 6已通过） */
    @TableField("status")
    private Byte status;

    /** 提交申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /** 受理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("accept_time")
    private LocalDateTime acceptTime;

    /** 办结时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("finish_time")
    private LocalDateTime finishTime;

    /** 驳回原因/退回原因 */
    @TableField("reject_reason")
    private String rejectReason;

    /** 是否加急（0普通 1加急） */
    @TableField("urgency")
    private Byte urgency;

    /** 版本号（乐观锁，防并发操作冲突） */
    @TableField("version")
    private Integer version;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 删除标志（0存在 1删除） */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
