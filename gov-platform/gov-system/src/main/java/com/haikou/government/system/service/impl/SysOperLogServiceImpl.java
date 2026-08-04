package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.system.domain.SysOperLog;
import com.haikou.government.system.mapper.SysOperLogMapper;
import com.haikou.government.system.service.SysOperLogService;
import com.haikou.government.system.vo.OperLogVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 查询操作日志列表（按时间倒序）
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
     * 实体转VO
     */
    private OperLogVO convertToVO(SysOperLog operLog) {
        OperLogVO vo = new OperLogVO();
        BeanUtils.copyProperties(operLog, vo);
        return vo;
    }
}
