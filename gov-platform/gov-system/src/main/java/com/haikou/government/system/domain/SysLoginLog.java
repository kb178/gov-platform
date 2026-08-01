package com.haikou.government.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haikou.government.common.core.domain.BaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 登录日志表
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
@Getter
@Setter
@TableName("sys_login_log")
public class SysLoginLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId("info_id")
    private Long infoId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 登录IP
     */
    @TableField("ipaddr")
    private String ipaddr;

    /**
     * 登录地点
     */
    @TableField("login_location")
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @TableField("browser")
    private String browser;

    /**
     * 操作系统
     */
    @TableField("os")
    private String os;

    /**
     * 登录状态（0成功 1失败）
     */
    @TableField("status")
    private Byte status;

    /**
     * 提示消息
     */
    @TableField("msg")
    private String msg;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;
}
