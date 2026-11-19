package com.haikou.government.item.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事项收藏表
 *
 * 注意：不继承 BaseEntity，收藏记录只需记录谁收藏了什么和收藏时间
 *
 * @author xhl
 * @since 2026-08-10
 */
@Data
@TableName("item_favorite")
public class ItemFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 事项ID */
    @TableField("item_id")
    private Long itemId;

    /** 收藏时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private LocalDateTime createTime;
}
