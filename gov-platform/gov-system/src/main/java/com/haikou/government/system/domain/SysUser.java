package com.haikou.government.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haikou.government.common.core.domain.BaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author xhl
 * @since 2026-08-01
 */
@Getter
@Setter
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 身份证号
     */
    @TableField("id_card")
    private String idCard;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 性别（0未知 1男 2女）
     */
    @TableField("sex")
    private Byte sex;

    /**
     * 头像地址
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 用户类型（1老百姓 2工作人员）
     */
    @TableField("user_type")
    private Byte userType;

    /**
     * 部门ID（工作人员才有）
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 状态（0正常 1停用）
     */
    @TableField("status")
    private Byte status;

    /**
     * 实名认证状态（0未认证 1已认证）
     */
    @TableField("real_name_status")
    private Byte realNameStatus;

    /**
     * 最后登录IP
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;

    /**
     * 删除标志（0存在 1删除）
     */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
