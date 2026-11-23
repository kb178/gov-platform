package com.haikou.government.approval.vo;

import com.haikou.government.approval.domain.ApprovalRecord;
import com.haikou.government.approval.domain.ApplicationMaterial;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 申请单详情 VO（含表单数据、材料清单、审批记录）
 *
 * @author xhl
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationDetailVO extends ApplicationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 表单模板ID */
    private Long templateId;

    /** 表单数据（JSON 字符串） */
    private String formData;

    /** 材料清单 */
    private List<ApplicationMaterial> materials;

    /** 审批记录 */
    private List<ApprovalRecord> records;
}
