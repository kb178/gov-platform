package com.haikou.government.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 实名认证 DTO（Data Transfer Object）
 *
 * 作用：封装实名认证请求的参数
 * 使用场景：用户提交身份证号和姓名进行实名认证
 *
 * @author xhl
 * @date 2026-08-02
 */
@Schema(description = "实名认证请求参数")
@Data
public class RealNameDTO {

    /**
     * 真实姓名
     *
     * 校验规则：
     * - @NotBlank: 不能为空
     * - 长度限制：2-20个字符
     */
    @Schema(description = "真实姓名", required = true, example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 身份证号
     *
     * 校验规则：
     * - @NotBlank: 不能为空
     * - @Pattern: 必须符合身份证号格式（18位，最后一位可以是X）
     */
    @Schema(description = "身份证号", required = true, example = "460100199001011234")
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;
}
