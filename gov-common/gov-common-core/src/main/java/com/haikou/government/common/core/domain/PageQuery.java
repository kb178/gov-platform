package com.haikou.government.common.core.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数
 *
 * @author gov-platform
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码（从1开始） */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 排序字段 */
    private String orderByColumn;

    /** 排序方式 asc/desc */
    private String isAsc;

    /**
     * 获取安全的页码
     */
    public int getPageNum() {
        return pageNum != null && pageNum > 0 ? pageNum : 1;
    }

    /**
     * 获取安全的每页条数
     */
    public int getPageSize() {
        if (pageSize == null || pageSize <= 0) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
