package com.haikou.government.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 材料模板 VO（响应）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "材料模板VO")
public class ItemMaterialTemplateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "材料ID")
    private Long materialId;

    @Schema(description = "材料名称")
    private String materialName;

    @Schema(description = "材料说明")
    private String materialDesc;

    @Schema(description = "材料类型（0纸质 1电子版 2两者都要）")
    private Byte materialType;

    @Schema(description = "材料类型文本")
    private String materialTypeText;

    @Schema(description = "示例图片URL")
    private String exampleUrl;

    @Schema(description = "状态（0正常 1停用）")
    private Byte status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
