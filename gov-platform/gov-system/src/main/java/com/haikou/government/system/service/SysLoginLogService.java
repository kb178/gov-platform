package com.haikou.government.system.service;

import com.haikou.government.common.core.domain.PageResult;
import com.haikou.government.system.domain.SysLoginLog;
import com.haikou.government.system.dto.LoginLogQueryDTO;
import com.haikou.government.system.vo.LoginLogVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 登录日志表 服务接口
 *
 * @author xhl
 * @since 2026-08-01
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志
     *
     * @param queryDTO 查询参数（包含分页和条件）
     * @return 分页结果
     */
    PageResult<LoginLogVO> getLoginLogPage(LoginLogQueryDTO queryDTO);

    /**
     * 查询登录日志列表（不分页）
     *
     * @return 登录日志列表
     */
    List<LoginLogVO> getLoginLogList();

    /**
     * 查询登录日志详情
     *
     * @param infoId 日志ID
     * @return 登录日志信息
     */
    LoginLogVO getLoginLogById(Long infoId);

    /**
     * 清空登录日志
     *
     * @return 是否成功
     */
    boolean cleanLoginLog();
}
