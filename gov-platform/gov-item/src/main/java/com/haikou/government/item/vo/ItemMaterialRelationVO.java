package com.haikou.government.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 事项材料关联 VO（响应）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "事项材料关联VO")
public class ItemMaterialRelationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联ID")
    private Long id;

    @Schema(description = "事项ID")
    private Long itemId;

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

    @Schema(description = "是否必须（0否 1是）")
    private Byte required;

    @Schema(description = "是否必须文本")
    private String requiredText;

    @Schema(description = "排序号")
    private Integer sortNum;

    @Schema(description = "备注")
    private String remark;
}
