package com.haikou.government.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应结果
 *
 * @author 海口政务开发团队
 * @since 2026-08-01
 */
@Schema(description = "用户信息响应结果")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    @Schema(description = "用户ID", example = "2083862615502741505")
    private Long userId;

    @Schema(description = "手机号（脱敏）", example = "138****8000")
    private String phone;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "性别（0未知 1男 2女）", example = "1")
    private Byte sex;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "实名状态（0未认证 1已认证）", example = "1")
    private Byte realNameStatus;

    @Schema(description = "真实姓名（脱敏）", example = "张*三")
    private String realName;
}
