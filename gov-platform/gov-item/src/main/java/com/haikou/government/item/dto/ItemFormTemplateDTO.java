package com.haikou.government.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 表单模板 DTO（请求）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "表单模板DTO")
public class ItemFormTemplateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模板ID（修改时必填）")
    private Long templateId;

    @NotNull(message = "事项ID不能为空")
    @Schema(description = "事项ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long itemId;

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 128, message = "模板名称长度不能超过128个字符")
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateName;

    @Schema(description = "表单字段配置（JSON格式）")
    private String formConfig;

    @Schema(description = "状态（0草稿 1已发布）")
    private Byte status;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注")
    private String remark;
}
