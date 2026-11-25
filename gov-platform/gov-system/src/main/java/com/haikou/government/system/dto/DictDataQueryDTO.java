package com.haikou.government.system.dto;

import com.haikou.government.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据查询参数
 *
 * @author xhl
 * @since 2026-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataQueryDTO extends PageQuery {

    /** 字典类型 */
    private String dictType;

    /** 字典标签 */
    private String dictLabel;

    /** 状态（0正常 1停用） */
    private Byte status;
}
