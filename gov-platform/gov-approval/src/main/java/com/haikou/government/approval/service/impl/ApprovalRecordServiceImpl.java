package com.haikou.government.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haikou.government.approval.domain.ApprovalRecord;
import com.haikou.government.approval.mapper.ApprovalRecordMapper;
import com.haikou.government.approval.service.ApprovalRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审批记录 服务实现类
 *
 * @author xhl
 * @since 2026-11-23
 */
@Slf4j
@Service
public class ApprovalRecordServiceImpl extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord> implements ApprovalRecordService {

    /**
     * 查询某个申请的全部审批记录（按时间正序）
     */
    @Override
    public List<ApprovalRecord> getRecordsByApplicationId(Long applicationId) {
        return this.list(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getApplicationId, applicationId)
                .orderByAsc(ApprovalRecord::getCreateTime));
    }
}
