package com.haikou.government.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haikou.government.approval.domain.Application;
import com.haikou.government.approval.domain.ApplicationFormData;
import com.haikou.government.approval.domain.ApplicationMaterial;
import com.haikou.government.approval.dto.ApplicationSubmitDTO;
import com.haikou.government.approval.mapper.ApplicationFormDataMapper;
import com.haikou.government.approval.mapper.ApplicationMapper;
import com.haikou.government.approval.mapper.ApplicationMaterialMapper;
import com.haikou.government.approval.service.ApprovalRecordService;
import com.haikou.government.approval.service.ApplicationService;
import com.haikou.government.approval.vo.ApplicationDetailVO;
import com.haikou.government.approval.vo.ApplicationVO;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.haikou.government.api.remote.RemoteItemService;
import com.haikou.government.common.core.domain.R;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.redis.utils.RedisUtils;
import com.haikou.government.common.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 申请单 服务实现类
 *
 * @author xhl
 * @since 2026-11-23
 */
@Slf4j
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    @Autowired
    private ApplicationFormDataMapper formDataMapper;

    @Autowired
    private ApplicationMaterialMapper materialMapper;

    @Autowired
    private ApprovalRecordService approvalRecordService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RemoteItemService remoteItemService;

    /** 申请编号序号 Redis key 前缀 */
    private static final String APP_NO_SEQ_PREFIX = "approval:app:no:seq:";

    // ==================== 查询 ====================

    /**
     * 分页查询申请单列表
     */
    @Override
    public IPage<ApplicationVO> getApplicationPage(Page<ApplicationVO> page, Long userId, Byte status) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Application::getUserId, userId)
                .eq(status != null, Application::getStatus, status)
                .orderByDesc(Application::getCreateTime);

        Page<Application> entityPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<Application> result = this.page(entityPage, wrapper);

        Page<ApplicationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        //通过convertToVO把Application转为Application
        List<ApplicationVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询申请单详情（含表单数据、材料清单、审批记录）
     */
    @Override
    public ApplicationDetailVO getApplicationDetail(Long applicationId) {
        Application application = this.getById(applicationId);
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }

        ApplicationDetailVO vo = new ApplicationDetailVO();
        BeanUtils.copyProperties(application, vo);
        vo.setStatusText(getStatusText(application.getStatus()));

        // 表单数据
        ApplicationFormData formData = formDataMapper.selectOne(
                new LambdaQueryWrapper<ApplicationFormData>()
                        .eq(ApplicationFormData::getApplicationId, applicationId));
        if (formData != null) {
            vo.setTemplateId(formData.getTemplateId());
            vo.setFormData(formData.getFormData());
        }

        // 材料清单（必填的排前面）
        List<ApplicationMaterial> materials = materialMapper.selectList(
                new LambdaQueryWrapper<ApplicationMaterial>()
                        .eq(ApplicationMaterial::getApplicationId, applicationId)
                        .orderByAsc(ApplicationMaterial::getRequired)
                        .orderByAsc(ApplicationMaterial::getId));
        vo.setMaterials(materials);

        // 审批记录
        vo.setRecords(approvalRecordService.getRecordsByApplicationId(applicationId));

        return vo;
    }

    // ==================== 提交申请 ====================

    /**
     * 提交申请（创建申请单 + 表单数据 + 材料清单）
     *
     * 事务控制：3 张表的操作要么全部成功，要么全部回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitApplication(ApplicationSubmitDTO dto, Long userId) {
        if (!StringUtils.hasText(dto.getFormData())) {
            throw new BusinessException("表单数据不能为空");
        }
        if (dto.getMaterials() == null || dto.getMaterials().isEmpty()) {
            throw new BusinessException("申请材料不能为空");
        }

        // 同一事项有在途申请（待受理/审核中/退回/审批中）就不让再提
        Long pendingCount = this.count(new LambdaQueryWrapper<Application>()
                .eq(Application::getUserId, userId)
                .eq(Application::getItemId, dto.getItemId())
                .in(Application::getStatus, 1, 2, 3, 4));
        if (pendingCount > 0) {
            throw new BusinessException("您对该事项已有在途申请，请勿重复提交");
        }

        // 调 gov-item 校验材料完整性
        checkMaterialCompleteness(dto);

        // 创建申请单
        Application application = buildApplication(dto, userId);
        application.setStatus((byte) 1); // 待受理
        application.setApplyTime(LocalDateTime.now());
        this.save(application);

        saveFormData(application.getApplicationId(), dto);
        saveMaterials(application.getApplicationId(), dto);

        log.info("提交申请成功: applicationNo={}, userId={}, itemId={}",
                application.getApplicationNo(), userId, dto.getItemId());

        return application.getApplicationId();
    }

    /**
     * 保存草稿
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(ApplicationSubmitDTO dto, Long userId) {
        Application application = buildApplication(dto, userId);
        application.setStatus((byte) 0); // 草稿
        this.save(application);

        // 草稿的表单和材料可以为空
        if (StringUtils.hasText(dto.getFormData())) {
            saveFormData(application.getApplicationId(), dto);
        }
        if (dto.getMaterials() != null && !dto.getMaterials().isEmpty()) {
            saveMaterials(application.getApplicationId(), dto);
        }

        log.info("保存草稿成功: applicationNo={}, userId={}", application.getApplicationNo(), userId);
        return application.getApplicationId();
    }

    /**
     * 修改草稿
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(Long applicationId, ApplicationSubmitDTO dto, Long userId) {
        Application existing = checkOwnership(applicationId, userId);
        if (existing.getStatus() == null || existing.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的申请才能修改");
        }

        Application update = new Application();
        update.setApplicationId(applicationId);
        update.setItemId(dto.getItemId());
        update.setUrgency(dto.getUrgency());
        update.setRemark(dto.getRemark());
        this.updateById(update);

        // 表单和材料先删后增
        formDataMapper.delete(new LambdaQueryWrapper<ApplicationFormData>()
                .eq(ApplicationFormData::getApplicationId, applicationId));
        if (StringUtils.hasText(dto.getFormData())) {
            saveFormData(applicationId, dto);
        }

        materialMapper.delete(new LambdaQueryWrapper<ApplicationMaterial>()
                .eq(ApplicationMaterial::getApplicationId, applicationId));
        if (dto.getMaterials() != null && !dto.getMaterials().isEmpty()) {
            saveMaterials(applicationId, dto);
        }

        log.info("修改草稿成功: applicationId={}, userId={}", applicationId, userId);
    }

    /**
     * 删除草稿（逻辑删除）
     */
    @Override
    public void deleteDraft(Long applicationId, Long userId) {
        Application existing = checkOwnership(applicationId, userId);

        if (existing.getStatus() == null || existing.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的申请才能删除");
        }

        this.removeById(applicationId);
        log.info("删除草稿成功: applicationId={}, userId={}", applicationId, userId);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建申请单对象（生成申请编号）
     */
    private Application buildApplication(ApplicationSubmitDTO dto, Long userId) {
        Application application = new Application();
        application.setApplicationNo(generateApplicationNo());
        application.setUserId(userId);
        application.setItemId(dto.getItemId());
        application.setUrgency(dto.getUrgency() != null ? dto.getUrgency() : (byte) 0);
        application.setRemark(dto.getRemark());
        application.setVersion(0);
        return application;
    }

    /**
     * 生成申请编号：SQ + 年月日 + 4位序号，如 SQ202611230001
     * 用 Redis INCR 保证并发下编号不重复
     */
    private String generateApplicationNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqKey = APP_NO_SEQ_PREFIX + dateStr;

        Long seq = redisUtils.increment(seqKey);
        redisUtils.expire(seqKey, 48, java.util.concurrent.TimeUnit.HOURS);

        return "SQ" + dateStr + String.format("%04d", seq);
    }

    private void saveFormData(Long applicationId, ApplicationSubmitDTO dto) {
        ApplicationFormData formData = new ApplicationFormData();
        formData.setApplicationId(applicationId);
        formData.setTemplateId(dto.getTemplateId());
        formData.setFormData(dto.getFormData());
        formDataMapper.insert(formData);
    }

    private void saveMaterials(Long applicationId, ApplicationSubmitDTO dto) {
        for (ApplicationSubmitDTO.MaterialItem item : dto.getMaterials()) {
            ApplicationMaterial material = new ApplicationMaterial();
            material.setApplicationId(applicationId);
            material.setMaterialId(item.getMaterialId());
            material.setMaterialName(item.getMaterialName());
            material.setRequired(item.getRequired() != null ? item.getRequired() : (byte) 1);
            material.setFileUrl(item.getFileUrl());
            material.setCheckStatus((byte) 0);
            materialMapper.insert(material);
        }
    }

    private Application checkOwnership(Long applicationId, Long userId) {
        Application application = this.getById(applicationId);
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }
        if (!application.getUserId().equals(userId)) {
            throw new BusinessException("无权操作他人的申请");
        }
        return application;
    }

    /**
     * 校验材料完整性（调 gov-item 查事项配置的必需材料）
     * 调不通时不阻塞提交，只记日志（降级处理）
     * 用 SphU.entry 编程式埋点，让 Sentinel 能看到这个资源（同类内部调用 @SentinelResource 不生效）
     */
    private void checkMaterialCompleteness(ApplicationSubmitDTO dto) {
        Entry entry = null;
        try {
            entry = SphU.entry("checkMaterial"); //埋雷让sentinel可以找到，也标志者要开始进行远程调用了
            R<List<Map<String, Object>>> result = remoteItemService.getMaterialListByItemId(dto.getItemId()); //远程调用
            if (result == null || !result.isSuccess() || result.getData() == null) {
                // 远程调用业务失败（比如 gov-item 抛异常返回 500），也要告诉 Sentinel 这次是失败的
                Tracer.trace(new RuntimeException("材料清单查询失败"));
                log.warn("查询事项材料清单失败，跳过完整性校验: itemId={}", dto.getItemId());
                return;
            }

            // 查出该事项配置的必需材料
            List<Map<String, Object>> requiredMaterials = result.getData().stream()
                    .filter(m -> {
                        Object required = m.get("required");
                        return required != null && "1".equals(String.valueOf(required));
                    })
                    .collect(Collectors.toList());

            if (requiredMaterials.isEmpty()) {
                return;
            }

            // 用户传了哪些材料ID
            Set<Long> submittedIds = dto.getMaterials().stream()
                    .map(ApplicationSubmitDTO.MaterialItem::getMaterialId)
                    .collect(Collectors.toSet());

            // 找出缺少的必需材料
            List<String> missing = new ArrayList<>();
            for (Map<String, Object> m : requiredMaterials) {
                Long materialId = Long.valueOf(String.valueOf(m.get("materialId")));
                if (!submittedIds.contains(materialId)) {
                    String name = String.valueOf(m.get("materialName"));
                    missing.add(name);
                }
            }

            if (!missing.isEmpty()) {
                throw new BusinessException("缺少必需材料：" + String.join("、", missing));
            }
        } catch (BlockException e) {
            // 被 Sentinel 限流/熔断了，跳过校验
            log.warn("checkMaterial 被限流/熔断，跳过校验: {}", e.getMessage());
        } catch (BusinessException e) {
            Tracer.trace(e);  // 告诉 Sentinel：这次失败了，记入异常统计
            throw e;
        } catch (Exception e) {
            Tracer.trace(e);  // 告诉 Sentinel：这次失败了，记入异常统计
            log.warn("调用 gov-item 校验材料失败，跳过校验: {}", e.getMessage());
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private ApplicationVO convertToVO(Application application) {
        ApplicationVO vo = new ApplicationVO();
        BeanUtils.copyProperties(application, vo);
        vo.setStatusText(getStatusText(application.getStatus()));
        return vo;
    }

    private String getStatusText(Byte status) {
        if (status == null) return "未知";
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
