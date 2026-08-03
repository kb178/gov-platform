package com.haikou.government.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应 VO（View Object）
 *
 * 作用：封装登录成功后返回给前端的数据
 * 为什么用 VO：VO 专门用于返回给前端展示的数据，与实体类解耦
 *
 * @author xhl
 * @since 2026-08-01
 */
@Schema(description = "登录响应结果")
@Data
@Builder // 使用 Builder 模式，方便构建对象
public class LoginVO {

    /**
     * 访问令牌（JWT Token）
     *
     * JWT Token 的作用：
     * 1. 用户登录成功后，后端生成 Token 返回给前端
     * 2. 前端后续请求时，在 Header 中携带此 Token
     * 3. 后端通过解析 Token 来验证用户身份，无需每次查询数据库
     *
     * Token 格式：Header.Payload.Signature（三段用 . 分隔）
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "13800138000")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 用户类型（1=老百姓，2=工作人员）
     */
    @Schema(description = "用户类型（1=老百姓，2=工作人员）", example = "1")
    private Byte userType;
}
