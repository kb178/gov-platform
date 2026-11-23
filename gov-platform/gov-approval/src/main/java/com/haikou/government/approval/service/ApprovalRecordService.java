package com.haikou.government.approval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.haikou.government.approval.domain.ApprovalRecord;

import java.util.List;

/**
 * 审批记录 服务接口
 *
 * @author xhl
 */
public interface ApprovalRecordService extends IService<ApprovalRecord> {

    /**
     * 查询某个申请的全部审批记录（按时间正序）
     *
     * @param applicationId 申请单ID
     * @return 审批记录列表
     */
    List<ApprovalRecord> getRecordsByApplicationId(Long applicationId);
}
