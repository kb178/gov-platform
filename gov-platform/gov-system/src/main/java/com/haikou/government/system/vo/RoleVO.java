package com.haikou.government.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色视图对象
 *
 * @author xhl
 * @since 2026-08-03
 */
@Schema(description = "角色信息响应")
@Data
public class RoleVO {

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色标识")
    private String roleKey;

    @Schema(description = "排序")
    private Integer sortNum;

    @Schema(description = "数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）")
    private Byte dataScope;

    @Schema(description = "状态（0正常 1停用）")
    private Byte status;

    @Schema(description = "备注")
    private String remark;

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

    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;
}
