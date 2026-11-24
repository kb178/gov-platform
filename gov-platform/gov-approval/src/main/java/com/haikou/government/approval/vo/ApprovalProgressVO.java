package com.haikou.government.approval.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批进度 VO（时间线）
 *
 * 前端展示为时间线：
 * 2026-11-23 10:00  张三 提交申请
 * 2026-11-23 14:30  李四 受理     "材料齐全"
 * 2026-11-24 09:00  王五 审核通过   "符合条件"
 *
 * @author xhl
 */
@Data
public class ApprovalProgressVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 申请单ID */
    private Long applicationId;

    /** 申请编号 */
    private String applicationNo;

    /** 当前状态 */
    private Byte status;

    /** 当前状态文本 */
    private String statusText;

    /** 时间线节点列表 */
    private List<TimeLineNode> timeLine;

    /**
     * 时间线节点
     */
    @Data
    public static class TimeLineNode implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 节点名称（提交申请/窗口受理/科室审核/领导审批） */
        private String nodeName;

        /** 操作人 */
        private String operatorName;

        /** 操作描述（提交申请/受理/通过/驳回/退回） */
        private String actionText;

        /** 审批意见 */
        private String opinion;

        /** 操作时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime time;

        /** 是否当前节点（前端高亮用） */
        private Boolean current;
    }
}
