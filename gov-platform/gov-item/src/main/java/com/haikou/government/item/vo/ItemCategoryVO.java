package com.haikou.government.item.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事项分类视图对象
 *
 * @author xhl
 * @since 2026-08-10
 */
@Schema(description = "事项分类信息响应")
@Data
public class ItemCategoryVO {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "父分类ID（0表示顶级分类）")
    private Long parentId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "分类图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sortNum;

    @Schema(description = "状态（0正常 1停用）")
    private Byte status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "该分类下的事项数量")
    private Integer itemCount;

    @Schema(description = "创建者")
    private String createBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    private String updateBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "子分类列表")
    private List<ItemCategoryVO> children;
}
