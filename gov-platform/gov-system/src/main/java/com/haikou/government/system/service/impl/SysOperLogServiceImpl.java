package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.system.domain.SysOperLog;
import com.haikou.government.system.dto.OperLogQueryDTO;
import com.haikou.government.system.mapper.SysOperLogMapper;
import com.haikou.government.system.service.SysOperLogService;
import com.haikou.government.system.vo.OperLogVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志表 服务实现类
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    /**
     * 分页查询操作日志
     */
    @Override
    public PageResult<OperLogVO> getOperLogPage(OperLogQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<SysOperLog> wrapper = buildQueryWrapper(queryDTO);

        // 执行分页查询
        Page<SysOperLog> page = this.page(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                wrapper
        );

        // 转换为 VO
        List<OperLogVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 查询操作日志列表（不分页）
     */
    @Override
    public List<OperLogVO> getOperLogList() {
        List<SysOperLog> list = this.list(new LambdaQueryWrapper<SysOperLog>()
                .orderByDesc(SysOperLog::getOperTime));
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询操作日志详情
     */
    @Override
    public OperLogVO getOperLogById(Long operId) {
        SysOperLog operLog = this.getById(operId);
        if (operLog == null) {
            throw new BusinessException("操作日志不存在");
        }
        return convertToVO(operLog);
    }

    /**
     * 清空操作日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cleanOperLog() {
        this.remove(new LambdaQueryWrapper<>());
        log.info("清空操作日志成功");
        return true;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<SysOperLog> buildQueryWrapper(OperLogQueryDTO queryDTO) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();

        // 模块标题（模糊查询）
        if (StringUtils.hasText(queryDTO.getTitle())) {
            wrapper.like(SysOperLog::getTitle, queryDTO.getTitle());
        }

        // 操作人员（模糊查询）
        if (StringUtils.hasText(queryDTO.getOperName())) {
            wrapper.like(SysOperLog::getOperName, queryDTO.getOperName());
        }

        // 操作状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysOperLog::getStatus, queryDTO.getStatus());
        }

        // 时间范围
        if (queryDTO.getBeginTime() != null) {
            wrapper.ge(SysOperLog::getOperTime, queryDTO.getBeginTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(SysOperLog::getOperTime, queryDTO.getEndTime());
        }

        // 按时间倒序
        wrapper.orderByDesc(SysOperLog::getOperTime);

        return wrapper;
    }

    /**
     * 实体转VO
     */
    private OperLogVO convertToVO(SysOperLog operLog) {
        OperLogVO vo = new OperLogVO();
        BeanUtils.copyProperties(operLog, vo);
        return vo;
    }
}
