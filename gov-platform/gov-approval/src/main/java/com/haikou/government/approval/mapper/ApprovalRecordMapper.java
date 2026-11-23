package com.haikou.government.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haikou.government.approval.domain.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批记录 Mapper
 *
 * @author xhl
 */
@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {
}
