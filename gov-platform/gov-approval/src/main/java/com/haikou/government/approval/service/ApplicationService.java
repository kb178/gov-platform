package com.haikou.government.approval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.haikou.government.approval.domain.Application;
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
     * 查询申请单详情
     *
     * @param applicationId 申请单ID
     * @return 申请单信息
     */
    ApplicationVO getApplicationDetail(Long applicationId);
}
