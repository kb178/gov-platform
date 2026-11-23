package com.haikou.government.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haikou.government.approval.domain.Application;
import com.haikou.government.approval.mapper.ApplicationMapper;
import com.haikou.government.approval.service.ApplicationService;
import com.haikou.government.approval.vo.ApplicationVO;
import com.haikou.government.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 申请单 服务实现类
 *
 * 第一阶段：只做基础 CRUD，业务逻辑（提交/审批/状态流转）在后续阶段实现
 *
 * @author xhl
 * @since 2026-11-23
 */
@Slf4j
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    /**
     * 分页查询申请单列表
     */
    @Override
    public IPage<ApplicationVO> getApplicationPage(Page<ApplicationVO> page, Long userId, Byte status) {
        // 构建查询条件
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Application::getUserId, userId)
                .eq(status != null, Application::getStatus, status)
                .orderByDesc(Application::getCreateTime);

        // 分页查询
        Page<Application> entityPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<Application> result = this.page(entityPage, wrapper);

        // 转换为 VO
        Page<ApplicationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ApplicationVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询申请单详情
     */
    @Override
    public ApplicationVO getApplicationDetail(Long applicationId) {
        Application application = this.getById(applicationId);
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }
        return convertToVO(application);
    }

    /**
     * 实体转 VO
     */
    private ApplicationVO convertToVO(Application application) {
        ApplicationVO vo = new ApplicationVO();
        BeanUtils.copyProperties(application, vo);

        // 状态文本
        vo.setStatusText(getStatusText(application.getStatus()));

        return vo;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Byte status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "草稿";
            case 1: return "待受理";
            case 2: return "审核中";
            case 3: return "退回补材料";
            case 4: return "审批中";
            case 5: return "已驳回";
            case 6: return "已通过";
            default: return "未知";
        }
    }
}
