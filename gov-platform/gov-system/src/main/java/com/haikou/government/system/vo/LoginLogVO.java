package com.haikou.government.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志视图对象
 *
 * @author xhl
 * @since 2026-08-04
 */
@Schema(description = "登录日志信息响应")
@Data
public class LoginLogVO {

    @Schema(description = "日志ID")
    private Long infoId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录IP")
    private String ipaddr;

    @Schema(description = "登录地点")
    private String loginLocation;

    @Schema(description = "浏览器")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "登录状态（0成功 1失败）")
    private Byte status;

    @Schema(description = "提示消息")
    private String msg;

    @Schema(description = "登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}
