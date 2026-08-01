package com.haikou.government.common.core.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类（纯POJO，不依赖任何框架）
 *
 * 只包含所有表都有的公共字段：
 * - createBy / createTime：创建审计
 * - updateBy / updateTime：更新审计
 * - delFlag：逻辑删除
 *
 * @author gov-platform
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 删除标志（0存在 1删除） */
    private Integer delFlag;
}
