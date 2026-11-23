package com.haikou.government.approval.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提交申请 DTO
 *
 * 用户提交申请时，一次性传入申请单信息 + 表单数据 + 材料清单
 *
 * @author xhl
 */
@Data
public class ApplicationSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 办理事项ID（item_info） */
    @NotNull(message = "办理事项不能为空")
    private Long itemId;

    /** 表单模板ID（item_form_template，可选） */
    private Long templateId;

    /** 表单数据（JSON 字符串，key=字段名，value=填写的值） */
    private String formData;

    /** 材料清单 */
    @Valid
    private List<MaterialItem> materials;

    /** 是否加急（0普通 1加急） */
    private Byte urgency;

    /** 备注 */
    private String remark;

    /**
     * 材料项（内部类）
     */
    @Data
    public static class MaterialItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 材料模板ID（item_material_template） */
        @NotNull(message = "材料ID不能为空")
        private Long materialId;

        /** 材料名称 */
        @NotBlank(message = "材料名称不能为空")
        private String materialName;

        /** 是否必须（0否 1是） */
        private Byte required;

        /** 材料文件URL */
        private String fileUrl;
    }
}
