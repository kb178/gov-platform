package com.haikou.government.approval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.haikou.government.approval.domain.Application;
import com.haikou.government.approval.dto.ApplicationSubmitDTO;
import com.haikou.government.approval.vo.ApplicationDetailVO;
import com.haikou.government.approval.vo.ApplicationVO;

/**
 * 申请单 服务接口
 *
 * @author xhl
 */
public interface ApplicationService extends IService<Application> {

    /**
     * 分页查询申请单列表
     *
     * @param page   分页参数
     * @param userId 申请人ID（可选，不传查全部）
     * @param status 状态（可选）
     * @return 分页结果
     */
    IPage<ApplicationVO> getApplicationPage(Page<ApplicationVO> page, Long userId, Byte status);

    /**
     * 查询申请单详情（含表单数据、材料清单、审批记录）
     *
     * @param applicationId 申请单ID
     * @return 申请单完整详情
     */
    ApplicationDetailVO getApplicationDetail(Long applicationId);

    /**
     * 提交申请（创建申请单 + 表单数据 + 材料清单）
     *
     * @param dto    提交参数
     * @param userId 申请人ID
     * @return 申请单ID
     */
    Long submitApplication(ApplicationSubmitDTO dto, Long userId);

    /**
     * 保存草稿
     *
     * @param dto    提交参数
     * @param userId 申请人ID
     * @return 申请单ID
     */
    Long saveDraft(ApplicationSubmitDTO dto, Long userId);

    /**
     * 修改草稿
     *
     * @param applicationId 申请单ID
     * @param dto           提交参数
     * @param userId        操作人ID（校验是否本人）
     */
    void updateDraft(Long applicationId, ApplicationSubmitDTO dto, Long userId);

    /**
     * 删除草稿（逻辑删除）
     *
     * @param applicationId 申请单ID
     * @param userId        操作人ID（校验是否本人）
     */
    void deleteDraft(Long applicationId, Long userId);
}
