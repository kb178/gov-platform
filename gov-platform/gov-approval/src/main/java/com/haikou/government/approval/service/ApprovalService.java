package com.haikou.government.approval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.approval.dto.ApprovalActionDTO;
import com.haikou.government.approval.dto.MaterialCheckDTO;
import com.haikou.government.approval.vo.ApprovalProgressVO;
import com.haikou.government.approval.vo.ApplicationVO;

/**
 * 审批操作 服务接口
 *
 * @author xhl
 */
public interface ApprovalService {

    /**
     * 受理（窗口人员）
     * 待受理(1) → 审核中(2)
     */
    void accept(ApprovalActionDTO dto, Long operatorId, String operatorName);

    /**
     * 不予受理（窗口人员）
     * 待受理(1) → 已驳回(5)
     */
    void rejectAtAccept(ApprovalActionDTO dto, Long operatorId, String operatorName);

    /**
     * 审核通过（科室）
     * 审核中(2) → 审批中(4)
     */
    void reviewPass(ApprovalActionDTO dto, Long operatorId, String operatorName);

    /**
     * 审批通过（领导）
     * 审批中(4) → 已通过(6)，触发办结
     */
    void approve(ApprovalActionDTO dto, Long operatorId, String operatorName);

    /**
     * 驳回（科室/领导）
     * 审核中(2) / 审批中(4) → 已驳回(5)
     */
    void reject(ApprovalActionDTO dto, Long operatorId, String operatorName);

    /**
     * 退回补材料（窗口/科室）
     * 待受理(1) / 审核中(2) → 退回(3)
     */
    void returnForMaterial(ApprovalActionDTO dto, Long operatorId, String operatorName);

    /**
     * 撤回申请（用户）
     * 待受理(1) → 草稿(0)
     */
    void withdraw(Long applicationId, Long userId);

    /**
     * 重新提交（用户，退回补材料后）
     * 退回(3) → 待受理(1)
     */
    void resubmit(Long applicationId, Long userId);

    /**
     * 待我审批列表（根据角色自动过滤）
     *
     * @param page   分页参数
     * @param userId 当前用户ID
     * @return 待办列表
     */
    IPage<ApplicationVO> getTodoList(Page<ApplicationVO> page, Long userId);

    /**
     * 已办列表（我处理过的）
     */
    IPage<ApplicationVO> getDoneList(Page<ApplicationVO> page, Long userId);

    /**
     * 材料核验（逐个材料标记通过/不通过）
     */
    void checkMaterials(MaterialCheckDTO dto, Long operatorId, String operatorName);

    /**
     * 查看审批进度时间线
     */
    ApprovalProgressVO getProgress(Long applicationId);
}
