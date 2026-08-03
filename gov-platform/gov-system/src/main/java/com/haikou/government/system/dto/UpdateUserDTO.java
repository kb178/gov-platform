package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人信息请求参数
 *
 * @author 海口政务开发团队
 * @since 2026-08-01
 */
@Schema(description = "修改个人信息请求参数")
@Data
public class UpdateUserDTO {

    @Schema(description = "昵称", example = "张三")
    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

    @Schema(description = "性别（0未知 1男 2女）", example = "1")
    private Byte sex;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;
}
