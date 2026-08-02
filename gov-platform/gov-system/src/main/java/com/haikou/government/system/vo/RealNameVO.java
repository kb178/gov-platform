package com.haikou.government.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 实名认证响应 VO（View Object）
 *
 * 作用：封装实名认证结果，返回给前端
 *
 * @author xhl
 * @date 2026-08-02
 */
@Schema(description = "实名认证响应结果")
@Data
@Builder
public class RealNameVO {

    /**
     * 认证状态
     * - 0: 未认证
     * - 1: 已认证
     */
    @Schema(description = "认证状态（0未认证 1已认证）", example = "1")
    private Byte status;

    /**
     * 真实姓名（脱敏）
     * 示例：张*三
     */
    @Schema(description = "真实姓名（脱敏）", example = "张*三")
    private String realName;

    /**
     * 身份证号（脱敏）
     * 示例：460100********1234
     */
    @Schema(description = "身份证号（脱敏）", example = "460100********1234")
    private String idCard;

    /**
     * 认证时间
     */
    @Schema(description = "认证时间", example = "2026-08-02 15:30:00")
    private String authTime;
}
