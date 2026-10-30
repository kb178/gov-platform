package com.haikou.government.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 事项材料关联 DTO（请求）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "事项材料关联DTO")
public class ItemMaterialRelationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "事项ID不能为空")
    @Schema(description = "事项ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long itemId;

    @Schema(description = "材料关联列表")
    private List<MaterialItem> materials;

    /**
     * 单个材料关联
     */
    @Data
    @Schema(description = "材料关联项")
    public static class MaterialItem {

        @NotNull(message = "材料ID不能为空")
        @Schema(description = "材料ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long materialId;

        @Schema(description = "是否必须（0否 1是）")
        private Byte required;

        @Schema(description = "排序号")
        private Integer sortNum;

        @Schema(description = "备注")
        private String remark;
    }
}
