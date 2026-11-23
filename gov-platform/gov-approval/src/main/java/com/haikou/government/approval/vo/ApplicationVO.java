package com.haikou.government.approval.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 申请单 VO
 *
 * @author xhl
 */
@Data
public class ApplicationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 申请单ID */
    private Long applicationId;

    /** 申请编号 */
    private String applicationNo;

    /** 申请人ID */
    private Long userId;

    /** 申请人姓名 */
    private String userName;

    /** 办理事项ID */
    private Long itemId;

    /** 事项名称 */
    private String itemName;

    /** 状态码 */
    private Byte status;

    /** 状态文本 */
    private String statusText;

    /** 提交申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /** 受理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;

    /** 办结时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /** 驳回原因/退回原因 */
    private String rejectReason;

    /** 是否加急 */
    private Byte urgency;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
