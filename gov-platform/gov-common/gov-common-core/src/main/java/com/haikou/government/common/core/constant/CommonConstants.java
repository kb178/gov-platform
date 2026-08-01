package com.haikou.government.common.core.constant;

/**
 * 通用常量
 *
 * @author gov-platform
 */
public class CommonConstants {

    /** 成功 */
    public static final int SUCCESS = 200;

    /** 失败 */
    public static final int FAIL = 500;

    /** 未认证 */
    public static final int UNAUTHORIZED = 401;

    /** 无权限 */
    public static final int FORBIDDEN = 403;

    /** 请求头 - Token */
    public static final String HEADER_TOKEN = "Authorization";

    /** Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 请求头 - 用户ID */
    public static final String HEADER_USER_ID = "X-User-Id";

    /** 请求头 - 用户名 */
    public static final String HEADER_USERNAME = "X-Username";

    /** Redis Key 前缀 - 用户Token */
    public static final String REDIS_TOKEN_PREFIX = "gov:token:";

    /** Redis Key 前缀 - 验证码 */
    public static final String REDIS_CAPTCHA_PREFIX = "gov:captcha:";

    /** Redis Key 前缀 - 用户信息缓存 */
    public static final String REDIS_USER_PREFIX = "gov:user:";

    /** 删除标志 - 正常 */
    public static final int DEL_FLAG_NORMAL = 0;

    /** 删除标志 - 已删除 */
    public static final int DEL_FLAG_DELETED = 1;

    /** 默认页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大每页条数 */
    public static final int MAX_PAGE_SIZE = 100;

    private CommonConstants() {
        // 私有构造，防止实例化
    }
}
