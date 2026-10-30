package com.haikou.government.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表单模板 VO（响应）
 *
 * @author xhl
 * @since 2026-08-13
 */
@Data
@Schema(description = "表单模板VO")
public class ItemFormTemplateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "事项ID")
    private Long itemId;

    @Schema(description = "事项名称")
    private String itemName;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "表单字段配置（JSON格式）")
    private String formConfig;

    @Schema(description = "状态（0草稿 1已发布）")
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
