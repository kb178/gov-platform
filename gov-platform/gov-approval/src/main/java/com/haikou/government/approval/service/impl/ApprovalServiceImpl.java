package com.haikou.government.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.approval.domain.ApprovalRecord;
import com.haikou.government.approval.domain.Application;
import com.haikou.government.approval.domain.ApplicationMaterial;
import com.haikou.government.approval.dto.ApprovalActionDTO;
import com.haikou.government.approval.dto.MaterialCheckDTO;
import com.haikou.government.approval.enums.ApprovalStatusEnum;
import com.haikou.government.approval.mapper.ApplicationMapper;
import com.haikou.government.approval.mapper.ApplicationMaterialMapper;
import com.haikou.government.approval.service.ApprovalRecordService;
import com.haikou.government.approval.service.ApprovalService;
import com.haikou.government.approval.vo.ApprovalProgressVO;
import com.haikou.government.approval.vo.ApplicationVO;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.common.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 审批操作 服务实现类
 *
 * 核心职责：
 * 1. 状态流转控制（只允许合法路径）
 * 2. 防并发（Redis 分布式锁 + 乐观锁）
 * 3. 写审批记录（audit trail）
 *
 * @author xhl
 * @since 2026-11-23
 */
@Slf4j
@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationMaterialMapper applicationMaterialMapper;

    @Autowired
    private ApprovalRecordService approvalRecordService;

    @Autowired
    private RedisUtils redisUtils;

    // ==================== 窗口人员操作 ====================

    /**
     * 受理：待受理(1) → 审核中(2)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(ApprovalActionDTO dto, Long operatorId, String operatorName) {
        // 先查状态，状态不对直接拦住，不用上锁
        Application app = getById(dto.getApplicationId());
        checkStatus(app, ApprovalStatusEnum.PENDING);

        // 状态没问题，再上锁防止并发
        String lockKey = acquireLock(dto.getApplicationId(), operatorId);
        try {
            updateStatus(app, ApprovalStatusEnum.REVIEWING);

            Application update = new Application();
            update.setApplicationId(app.getApplicationId());
            update.setAcceptTime(LocalDateTime.now());
            applicationMapper.updateById(update);

            saveRecord(dto.getApplicationId(), "窗口受理", operatorId, operatorName,
                    (byte) 1, dto.getOpinion());

            log.info("受理成功: applicationNo={}, operator={}", app.getApplicationNo(), operatorName);
        } finally {
            releaseLock(lockKey);
        }
    }

    /**
     * 不予受理：待受理(1) → 已驳回(5)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectAtAccept(ApprovalActionDTO dto, Long operatorId, String operatorName) {
        if (dto.getOpinion() == null || dto.getOpinion().trim().isEmpty()) {
            throw new BusinessException("不予受理必须填写原因");
        }

        Application app = getById(dto.getApplicationId());
        checkStatus(app, ApprovalStatusEnum.PENDING);

        String lockKey = acquireLock(dto.getApplicationId(), operatorId);
        try {
            updateStatus(app, ApprovalStatusEnum.REJECTED);

            Application update = new Application();
            update.setApplicationId(app.getApplicationId());
            update.setRejectReason(dto.getOpinion());
            applicationMapper.updateById(update);

            saveRecord(dto.getApplicationId(), "窗口受理", operatorId, operatorName,
                    (byte) 5, dto.getOpinion());

            log.info("不予受理: applicationNo={}, operator={}", app.getApplicationNo(), operatorName);
        } finally {
            releaseLock(lockKey);
        }
    }

    // ==================== 科室审核 ====================

    /**
     * 审核通过：审核中(2) → 审批中(4)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewPass(ApprovalActionDTO dto, Long operatorId, String operatorName) {
        Application app = getById(dto.getApplicationId());
        checkStatus(app, ApprovalStatusEnum.REVIEWING);

        String lockKey = acquireLock(dto.getApplicationId(), operatorId);
        try {
            updateStatus(app, ApprovalStatusEnum.APPROVING);

            saveRecord(dto.getApplicationId(), "科室审核", operatorId, operatorName,
                    (byte) 2, dto.getOpinion());

            log.info("科室审核通过: applicationNo={}, operator={}", app.getApplicationNo(), operatorName);
        } finally {
            releaseLock(lockKey);
        }
    }

    // ==================== 领导审批 ====================

    /**
     * 审批通过：审批中(4) → 已通过(6)，触发办结
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(ApprovalActionDTO dto, Long operatorId, String operatorName) {
        Application app = getById(dto.getApplicationId());
        checkStatus(app, ApprovalStatusEnum.APPROVING);

        String lockKey = acquireLock(dto.getApplicationId(), operatorId);
        try {
            updateStatus(app, ApprovalStatusEnum.APPROVED);

            Application update = new Application();
            update.setApplicationId(app.getApplicationId());
            update.setFinishTime(LocalDateTime.now());
            applicationMapper.updateById(update);

            saveRecord(dto.getApplicationId(), "领导审批", operatorId, operatorName,
                    (byte) 2, dto.getOpinion());

            log.info("审批通过，办结: applicationNo={}, operator={}", app.getApplicationNo(), operatorName);

            // TODO: 调 gov-message 发通知、调 gov-license 生成证照
        } finally {
            releaseLock(lockKey);
        }
    }

    // ==================== 驳回 / 退回 ====================

    /**
     * 驳回：审核中(2) / 审批中(4) → 已驳回(5)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(ApprovalActionDTO dto, Long operatorId, String operatorName) {
        if (dto.getOpinion() == null || dto.getOpinion().trim().isEmpty()) {
            throw new BusinessException("驳回必须填写原因");
        }

        Application app = getById(dto.getApplicationId());
        if (app == null) {
            throw new BusinessException("申请单不存在");
        }
        if (app.getStatus() == null
                || (app.getStatus() != ApprovalStatusEnum.REVIEWING.getCode()
                    && app.getStatus() != ApprovalStatusEnum.APPROVING.getCode())) {
            throw new BusinessException("当前状态不允许驳回");
        }

        String lockKey = acquireLock(dto.getApplicationId(), operatorId);
        try {
            updateStatus(app, ApprovalStatusEnum.REJECTED);

            Application update = new Application();
            update.setApplicationId(app.getApplicationId());
            update.setRejectReason(dto.getOpinion());
            applicationMapper.updateById(update);

            String nodeName = app.getStatus() == ApprovalStatusEnum.APPROVING.getCode()
                    ? "领导审批" : "科室审核";
            saveRecord(dto.getApplicationId(), nodeName, operatorId, operatorName,
                    (byte) 3, dto.getOpinion());

            log.info("驳回: applicationNo={}, operator={}", app.getApplicationNo(), operatorName);
        } finally {
            releaseLock(lockKey);
        }
    }

    /**
     * 退回补材料：待受理(1) / 审核中(2) → 退回(3)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnForMaterial(ApprovalActionDTO dto, Long operatorId, String operatorName) {
        if (dto.getOpinion() == null || dto.getOpinion().trim().isEmpty()) {
            throw new BusinessException("退回必须填写原因");
        }

        Application app = getById(dto.getApplicationId());
        if (app == null) {
            throw new BusinessException("申请单不存在");
        }
        if (app.getStatus() == null
                || (app.getStatus() != ApprovalStatusEnum.PENDING.getCode()
                    && app.getStatus() != ApprovalStatusEnum.REVIEWING.getCode())) {
            throw new BusinessException("当前状态不允许退回");
        }

        String lockKey = acquireLock(dto.getApplicationId(), operatorId);
        try {
            updateStatus(app, ApprovalStatusEnum.RETURNED);

            Application update = new Application();
            update.setApplicationId(app.getApplicationId());
            update.setRejectReason(dto.getOpinion());
            applicationMapper.updateById(update);

            String nodeName = app.getStatus() == ApprovalStatusEnum.PENDING.getCode()
                    ? "窗口受理" : "科室审核";
            saveRecord(dto.getApplicationId(), nodeName, operatorId, operatorName,
                    (byte) 4, dto.getOpinion());

            log.info("退回补材料: applicationNo={}, operator={}", app.getApplicationNo(), operatorName);
        } finally {
            releaseLock(lockKey);
        }
    }

    // ==================== 用户操作 ====================

    /**
     * 撤回：待受理(1) → 草稿(0)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long applicationId, Long userId) {
        Application app = getById(applicationId);
        if (app == null) {
            throw new BusinessException("申请单不存在");
        }
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException("只能撤回自己的申请");
        }
        checkStatus(app, ApprovalStatusEnum.PENDING);

        updateStatus(app, ApprovalStatusEnum.DRAFT);

        log.info("撤回申请: applicationNo={}, userId={}", app.getApplicationNo(), userId);
    }

    /**
     * 重新提交：退回(3) → 待受理(1)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resubmit(Long applicationId, Long userId) {
        Application app = getById(applicationId);
        if (app == null) {
            throw new BusinessException("申请单不存在");
        }
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException("只能操作自己的申请");
        }
        checkStatus(app, ApprovalStatusEnum.RETURNED);

        // 清掉旧的退回原因，更新状态
        int expectedVersion = app.getVersion() != null ? app.getVersion() : 0;
        LambdaUpdateWrapper<Application> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Application::getApplicationId, applicationId)
                .eq(Application::getVersion, expectedVersion)
                .set(Application::getStatus, (byte) ApprovalStatusEnum.PENDING.getCode())
                .set(Application::getApplyTime, LocalDateTime.now())
                .set(Application::getRejectReason, null) // 清掉旧退回原因
                .set(Application::getVersion, expectedVersion + 1);

        int rows = applicationMapper.update(null, wrapper);
        if (rows == 0) {
            throw new BusinessException("操作失败，数据已被其他人修改，请刷新后重试");
        }

        log.info("重新提交: applicationNo={}, userId={}", app.getApplicationNo(), userId);
    }

    // ==================== 查询 ====================

    /**
     * 待我审批列表
     *
     * 根据用户角色决定查什么：
     * - 窗口人员 → 待受理(1)
     * - 科室审核人 → 审核中(2)
     * - 领导 → 审批中(4)
     */
    @Override
    public IPage<ApplicationVO> getTodoList(Page<ApplicationVO> page, Long userId) {
        // 根据角色确定待办状态（这里简化处理，实际可以从 SecurityContext 拿角色）
        // TODO: 从用户角色动态判断，现在先让前端传 status 或者全查
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Application::getStatus, 1, 2, 4) // 待受理/审核中/审批中
                .orderByDesc(Application::getUrgency) // 加急的排前面
                .orderByAsc(Application::getApplyTime);

        Page<Application> entityPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<Application> result = applicationMapper.selectPage(entityPage, wrapper);

        Page<ApplicationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 已办列表（我处理过的，从审批记录反查）
     */
    @Override
    public IPage<ApplicationVO> getDoneList(Page<ApplicationVO> page, Long userId) {
        // 查我审批过的申请ID
        List<ApprovalRecord> myRecords = approvalRecordService.list(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getApproverId, userId));
        List<Long> appIds = myRecords.stream()
                .map(ApprovalRecord::getApplicationId)
                .distinct()
                .collect(Collectors.toList());

        if (appIds.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }

        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Application::getApplicationId, appIds)
                .orderByDesc(Application::getUpdateTime);

        Page<Application> entityPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<Application> result = applicationMapper.selectPage(entityPage, wrapper);

        Page<ApplicationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    // ==================== 材料核验 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkMaterials(MaterialCheckDTO dto, Long operatorId, String operatorName) {
        // 校验申请单存在且未办结
        Application app = getById(dto.getApplicationId());
        if (app == null) {
            throw new BusinessException("申请单不存在");
        }
        if (ApprovalStatusEnum.isFinal(app.getStatus())) {
            throw new BusinessException("已办结的申请不能核验材料");
        }

        for (MaterialCheckDTO.CheckItem item : dto.getItems()) {
            // 校验材料属于该申请
            ApplicationMaterial material = applicationMaterialMapper.selectById(item.getId());
            if (material == null || !material.getApplicationId().equals(dto.getApplicationId())) {
                throw new BusinessException("材料不存在或不属于该申请");
            }

            LambdaUpdateWrapper<ApplicationMaterial> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ApplicationMaterial::getId, item.getId())
                    .set(ApplicationMaterial::getCheckStatus, item.getCheckStatus())
                    .set(ApplicationMaterial::getCheckRemark, item.getCheckRemark())
                    .set(ApplicationMaterial::getCheckBy, operatorName)
                    .set(ApplicationMaterial::getCheckTime, LocalDateTime.now());
            applicationMaterialMapper.update(null, wrapper);
        }
        log.info("材料核验完成: applicationId={}, operator={}", dto.getApplicationId(), operatorName);
    }

    // ==================== 审批进度时间线 ====================

    @Override
    public ApprovalProgressVO getProgress(Long applicationId) {
        Application app = getById(applicationId);
        if (app == null) {
            throw new BusinessException("申请单不存在");
        }

        ApprovalProgressVO vo = new ApprovalProgressVO();
        vo.setApplicationId(app.getApplicationId());
        vo.setApplicationNo(app.getApplicationNo());
        vo.setStatus(app.getStatus());
        vo.setStatusText(ApprovalStatusEnum.getDesc(app.getStatus()));

        // 组装时间线：申请提交节点 + 审批记录节点
        List<ApprovalProgressVO.TimeLineNode> nodes = new ArrayList<>();

        // 第一个节点：提交申请
        ApprovalProgressVO.TimeLineNode submitNode = new ApprovalProgressVO.TimeLineNode();
        submitNode.setNodeName("提交申请");
        submitNode.setOperatorName(app.getCreateBy());
        submitNode.setActionText("提交申请");
        submitNode.setTime(app.getApplyTime());
        submitNode.setCurrent(false);
        nodes.add(submitNode);

        // 审批记录节点
        List<ApprovalRecord> records = approvalRecordService.getRecordsByApplicationId(applicationId);
        for (ApprovalRecord record : records) {
            ApprovalProgressVO.TimeLineNode node = new ApprovalProgressVO.TimeLineNode();
            node.setNodeName(record.getNodeName());
            node.setOperatorName(record.getApproverName());
            node.setActionText(getActionText(record.getAction()));
            node.setOpinion(record.getOpinion());
            node.setTime(record.getCreateTime());
            node.setCurrent(false);
            nodes.add(node);
        }

        // 最后一个节点标记为当前节点
        if (!nodes.isEmpty()) {
            nodes.get(nodes.size() - 1).setCurrent(true);
        }

        vo.setTimeLine(nodes);
        return vo;
    }

    private String getActionText(Byte action) {
        if (action == null) return "未知";
        switch (action) {
            case 1: return "受理";
            case 2: return "通过";
            case 3: return "驳回";
            case 4: return "退回补材料";
            case 5: return "不予受理";
            default: return "未知";
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 加分布式锁（防止两人同时操作同一个申请）
     */
    private String acquireLock(Long applicationId, Long operatorId) {
        String lockKey = "approval:lock:" + applicationId;
        boolean locked = redisUtils.setIfAbsent(lockKey, String.valueOf(operatorId), 30, TimeUnit.SECONDS);
        if (!locked) {
            throw new BusinessException("该申请正在被其他人处理，请稍后再试");
        }
        return lockKey;
    }

    private void releaseLock(String lockKey) {
        redisUtils.delete(lockKey);
    }

    /**
     * 校验当前状态
     */
    private void checkStatus(Application app, ApprovalStatusEnum expected) {
        if (app == null) {
            throw new BusinessException("申请单不存在");
        }
        if (app.getStatus() == null || app.getStatus() != expected.getCode()) {
            throw new BusinessException("当前状态是【" + ApprovalStatusEnum.getDesc(app.getStatus())
                    + "】，不允许此操作");
        }
    }

    /**
     * 更新状态（乐观锁）
     * WHERE application_id = ? AND version = ? — 版本对不上就更新失败
     */
    private void updateStatus(Application app, ApprovalStatusEnum target) {
        if (!ApprovalStatusEnum.canTransit(app.getStatus(), (byte) target.getCode())) {
            throw new BusinessException("不允许从【" + ApprovalStatusEnum.getDesc(app.getStatus())
                    + "】流转到【" + target.getDesc() + "】");
        }

        int expectedVersion = app.getVersion() != null ? app.getVersion() : 0;

        LambdaUpdateWrapper<Application> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Application::getApplicationId, app.getApplicationId())
                .eq(Application::getVersion, expectedVersion)
                .set(Application::getStatus, (byte) target.getCode())
                .set(Application::getVersion, expectedVersion + 1);

        int rows = applicationMapper.update(null, wrapper);
        if (rows == 0) {
            throw new BusinessException("操作失败，数据已被其他人修改，请刷新后重试");
        }
    }

    /**
     * 写审批记录
     */
    private void saveRecord(Long applicationId, String nodeName,
                            Long approverId, String approverName,
                            Byte action, String opinion) {
        ApprovalRecord record = new ApprovalRecord();
        record.setApplicationId(applicationId);
        record.setNodeName(nodeName);
        record.setApproverId(approverId);
        record.setApproverName(approverName);
        record.setAction(action);
        record.setOpinion(opinion);
        record.setCreateTime(LocalDateTime.now());
        approvalRecordService.save(record);
    }

    private Application getById(Long applicationId) {
        return applicationMapper.selectById(applicationId);
    }

    private ApplicationVO convertToVO(Application app) {
        ApplicationVO vo = new ApplicationVO();
        BeanUtils.copyProperties(app, vo);
        vo.setStatusText(ApprovalStatusEnum.getDesc(app.getStatus()));
        return vo;
    }
}
