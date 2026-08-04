package com.haikou.government.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.common.core.exception.BusinessException;
import com.haikou.government.system.domain.SysLoginLog;
import com.haikou.government.system.dto.LoginLogQueryDTO;
import com.haikou.government.system.mapper.SysLoginLogMapper;
import com.haikou.government.system.service.SysLoginLogService;
import com.haikou.government.system.vo.LoginLogVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志表 服务实现类
 *
 * @author xhl
 * @since 2026-08-01
 */
@Slf4j
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    /**
     * 分页查询登录日志
     */
    @Override
    public PageResult<LoginLogVO> getLoginLogPage(LoginLogQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<SysLoginLog> wrapper = buildQueryWrapper(queryDTO);

        // 执行分页查询
        Page<SysLoginLog> page = this.page(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                wrapper
        );

        // 转换为 VO
        List<LoginLogVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), voList, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 查询登录日志列表（不分页）
     */
    @Override
    public List<LoginLogVO> getLoginLogList() {
        List<SysLoginLog> list = this.list(new LambdaQueryWrapper<SysLoginLog>()
                .orderByDesc(SysLoginLog::getLoginTime));
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询登录日志详情
     */
    @Override
    public LoginLogVO getLoginLogById(Long infoId) {
        SysLoginLog loginLog = this.getById(infoId);
        if (loginLog == null) {
            throw new BusinessException("登录日志不存在");
        }
        return convertToVO(loginLog);
    }

    /**
     * 清空登录日志
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cleanLoginLog() {
        this.remove(new LambdaQueryWrapper<>());
        log.info("清空登录日志成功");
        return true;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<SysLoginLog> buildQueryWrapper(LoginLogQueryDTO queryDTO) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();

        // 用户名（模糊查询）
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(SysLoginLog::getUsername, queryDTO.getUsername());
        }

        // 登录状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysLoginLog::getStatus, queryDTO.getStatus());
        }

        // 时间范围
        if (queryDTO.getBeginTime() != null) {
            wrapper.ge(SysLoginLog::getLoginTime, queryDTO.getBeginTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(SysLoginLog::getLoginTime, queryDTO.getEndTime());
        }

        // 按时间倒序
        wrapper.orderByDesc(SysLoginLog::getLoginTime);

        return wrapper;
    }

    /**
     * 实体转VO
     */
    private LoginLogVO convertToVO(SysLoginLog loginLog) {
        LoginLogVO vo = new LoginLogVO();
        BeanUtils.copyProperties(loginLog, vo);
        return vo;
    }
}
