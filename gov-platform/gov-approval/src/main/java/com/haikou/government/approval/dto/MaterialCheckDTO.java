package com.haikou.government.approval.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 材料核验 DTO
 *
 * 窗口人员逐个材料标记核验结果
 *
 * @author xhl
 */
@Data
public class MaterialCheckDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 申请单ID */
    @NotNull(message = "申请单ID不能为空")
    private Long applicationId;

    /** 核验项列表 */
    @NotNull(message = "核验项不能为空")
    private List<CheckItem> items;

    @Data
    public static class CheckItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 材料ID（application_material 的 id） */
        @NotNull(message = "材料ID不能为空")
        private Long id;

        /** 核验结果（1通过 2不通过） */
        @NotNull(message = "核验结果不能为空")
        private Byte checkStatus;

        /** 核验备注（不通过时说明原因） */
        private String checkRemark;
    }
}
