package com.haikou.government.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 材料模板 DTO（请求）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "材料模板DTO")
public class ItemMaterialTemplateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "材料ID（修改时必填）")
    private Long materialId;

    @NotBlank(message = "材料名称不能为空")
    @Size(max = 100, message = "材料名称长度不能超过100个字符")
    @Schema(description = "材料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialName;

    @Size(max = 500, message = "材料说明长度不能超过500个字符")
    @Schema(description = "材料说明")
    private String materialDesc;

    @Schema(description = "材料类型（0纸质 1电子版 2两者都要）")
    private Byte materialType;

    @Size(max = 500, message = "示例图片URL长度不能超过500个字符")
    @Schema(description = "示例图片URL")
    private String exampleUrl;

    @Schema(description = "状态（0正常 1停用）")
    private Byte status;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注")
    private String remark;
}
