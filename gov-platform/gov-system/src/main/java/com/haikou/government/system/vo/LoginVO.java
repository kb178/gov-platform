package com.haikou.government.system.vo;

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
    private String accessToken;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户类型（1=老百姓，2=工作人员）
     */
    private Byte userType;
}
